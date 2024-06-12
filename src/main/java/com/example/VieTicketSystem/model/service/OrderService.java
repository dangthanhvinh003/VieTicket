package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.model.entity.*;
import com.example.VieTicketSystem.model.repo.OrderRepo;
import com.example.VieTicketSystem.model.repo.RefundOrderRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.repo.TicketRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final VNPayService vnPayService;
    private final ObjectMapper objectMapper;
    private final OrderRepo orderRepo;
    private final SeatRepo seatRepo;
    private final TicketRepo ticketRepo;
    private final RefundOrderRepo refundOrderRepo;
    private final EmailService emailService;
    private final QRCodeService qrCodeService;

    public OrderService(VNPayService vnPayService, ObjectMapper jacksonObjectMapper, OrderRepo orderRepo, SeatRepo seatRepo, TicketRepo ticketRepo, RefundOrderRepo refundOrderRepo, EmailService emailService, QRCodeService qrCodeService) {
        this.vnPayService = vnPayService;
        this.objectMapper = jacksonObjectMapper;
        this.orderRepo = orderRepo;
        this.seatRepo = seatRepo;
        this.ticketRepo = ticketRepo;
        this.refundOrderRepo = refundOrderRepo;
        this.emailService = emailService;
        this.qrCodeService = qrCodeService;
    }

    // Create order and return payment URL
    public String createOrder(List<Integer> selectedSeats, String orderInformation, String urlReturn, String clientIp, User user) throws Exception {

        long total = calculateTotalPrice(selectedSeats);
        Map<String, String> vnp_Params = new HashMap<>();
        String paymentURL = vnPayService.createOrder(total, orderInformation, urlReturn, clientIp, vnp_Params);

        // Persist vnpay data
        Map<String, String> vnPayPersist = new HashMap<>();
        vnPayPersist.put("vnp_TxnRef", vnp_Params.get("vnp_TxnRef"));
        vnPayPersist.put("vnp_CreateDate", vnp_Params.get("vnp_CreateDate"));

        // Save order to database
        Order order = new Order();
        order.setUser(user);
        order.setDate(LocalDateTime.now());
        order.setTotal(total);
        order.setStatus(Order.PaymentStatus.PENDING);
        order.setVnpayData(objectMapper.writeValueAsString(vnPayPersist));
        int orderId = orderRepo.save(order);

        // Generate pending tickets
        for (int seatId : selectedSeats) {
            ticketRepo.saveNew(UUID.randomUUID().toString(), null, orderId, seatId, Ticket.TicketStatus.PENDING);
        }

        return paymentURL;
    }

    // Handle payment response
    public Order handlePaymentResponse(Map<String, String> params) throws Exception {
        VNPayService.VNPayStatus vnPayStatus = vnPayService.orderReturn(params);
        Order.PaymentStatus orderStatus = vnPayStatus == VNPayService.VNPayStatus.SUCCESS ? Order.PaymentStatus.SUCCESS : Order.PaymentStatus.FAILED;

        String vnp_TxnRef = params.get("vnp_TxnRef");
        Order order = orderRepo.findByTxnRef(vnp_TxnRef);

        List<Ticket> tickets = ticketRepo.findByOrderId(order.getOrderId());
        List<Integer> ticketIds = tickets.stream().map(Ticket::getTicketId).collect(Collectors.toList());

        switch (orderStatus) {
            case SUCCESS -> {
                // Set payment status to success
                order.setStatus(Order.PaymentStatus.SUCCESS);
                orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.SUCCESS);

                // Update ticket status to success and set ticket purchase date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime payDate = LocalDateTime.parse(params.get("vnp_PayDate"), formatter);
                for (Ticket ticket : tickets) {
                    ticket.setStatus(Ticket.TicketStatus.PURCHASED);
                }
                ticketRepo.setSuccessInBulk(ticketIds, payDate, Ticket.TicketStatus.PURCHASED.toInteger());

                // Set seats to taken
                seatRepo.updateSeats(tickets.stream().map(ticket -> ticket.getSeat().getSeatId()).collect(Collectors.toList()), Seat.TakenStatus.TAKEN);

                // Send email to user
                String emailContent = createEmailContent(order, tickets);
                CompletableFuture<Void> future = emailService.sendEmail(order.getUser().getEmail(), "Order Confirmation", emailContent);
                future.exceptionally(ex -> {
                    // Handle the exception here
                    ex.printStackTrace();
                    return null;
                });
            }
            case FAILED -> {
                // Set payment status to failed
                order.setStatus(Order.PaymentStatus.FAILED);
                orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.FAILED);

                // Update ticket status to failed
                ticketRepo.setStatusInBulk(ticketIds, Ticket.TicketStatus.FAILED.toInteger());

                // Set seats to available
                seatRepo.updateSeats(tickets.stream().map(ticket -> ticket.getSeat().getSeatId()).collect(Collectors.toList()), Seat.TakenStatus.AVAILABLE);
            }
        }

        return order;
    }

    // Create email content
    private String createEmailContent(Order order, List<Ticket> tickets) throws IOException, WriterException {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<html><head><style>")
                .append(".ticket {border: 1px solid #000; margin: 10px; padding: 10px;}")
                .append("</style></head><body>");
        emailContent.append("<h1>Order Confirmation</h1>");
        emailContent.append("<p>Order ID: ").append(order.getOrderId()).append("</p>");
        emailContent.append("<p>Total: ").append(order.getTotal()).append("</p>");
        emailContent.append("<p>Status: ").append(order.getStatus()).append("</p>");
        emailContent.append("<h2>Tickets:</h2>");

        for (Ticket ticket : tickets) {
            emailContent.append("<div class='ticket'>");
            emailContent.append("<p>Lead visitor: ").append(ticket.getOrder().getUser().getFullName()).append("</p>");
            emailContent.append("<p>Event: ").append(ticket.getSeat().getRow().getArea().getEvent().getName()).append("</p>");
            emailContent.append("<p>Date&Time: ").append(ticket.getSeat().getRow().getArea().getEvent().getStartDate()).append(" - ").append(ticket.getSeat().getRow().getArea().getEvent().getEndDate()).append("</p>");
            emailContent.append("<p>Area: ").append(ticket.getSeat().getRow().getArea().getName()).append("</p>");
            emailContent.append("<p>Row: ").append(ticket.getSeat().getRow().getRowName()).append("</p>");
            emailContent.append("<p>Seat: ").append(ticket.getSeat().getNumber()).append("</p>");
            emailContent.append("<p>Price: ").append(ticket.getSeat().getTicketPrice()).append("</p>");
            emailContent.append("<p>QR Code:</p>");
            emailContent.append("<img src='data:image/png;base64,").append(qrCodeService.generateQRCodeImageBase64(ticket.getQrCode())).append("' />");
            emailContent.append("<p>QR Code Text: ").append(ticket.getQrCode()).append("</p>");
            emailContent.append("</div>");
        }

        emailContent.append("</body></html>");

        return emailContent.toString();
    }

    // Create refund order and request refund from VNPay
    public RefundOrder createRefundOrder(Order order, long amount) throws Exception {
        if (amount > order.getTotal()) {
            throw new IllegalArgumentException("Refund amount cannot exceed the total amount of the order");
        }

        // Get VNPay data
        Map<String, String> vnPayPersist = objectMapper.readValue(order.getVnpayData(),
                new TypeReference<>() {
                });

        // Create refund order
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setCreatedOn(LocalDateTime.now());
        refundOrder.setOrder(order);
        refundOrder.setStatus(RefundOrder.RefundStatus.PENDING);

        // Set order status to PENDING_REFUND
        order.setStatus(Order.PaymentStatus.PENDING_REFUND);
        orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.PENDING_REFUND);

        // Save refund order
        refundOrderRepo.save(refundOrder);

        // Request refund from VNPay
        vnPayService.refundOrder(vnPayPersist, amount)
                .doOnError(e -> {
                    refundOrder.setStatus(RefundOrder.RefundStatus.FAILED);
                    try {
                        refundOrderRepo.updateStatus(refundOrder);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .subscribe(response -> {
                    Map<String, String> responseMap;
                    try {
                        responseMap = objectMapper.readValue(response, new TypeReference<>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if (vnPayService.refundResponse(responseMap) == VNPayService.VNPayStatus.SUCCESS) {
                        order.setStatus(Order.PaymentStatus.REFUNDED);
                        refundOrder.setStatus(RefundOrder.RefundStatus.SUCCESS);
                        try {
                            orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.REFUNDED);
                            ticketRepo.updateStatusByOrderId(order.getOrderId(), Ticket.TicketStatus.RETURNED.toInteger());
                            seatRepo.updateSeats(ticketRepo.findByOrderId(order.getOrderId()).stream().map(ticket -> ticket.getSeat().getSeatId()).collect(Collectors.toList()), Seat.TakenStatus.AVAILABLE);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        refundOrder.setStatus(RefundOrder.RefundStatus.FAILED);
                    }
                    try {
                        refundOrderRepo.updateStatus(refundOrder);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        return refundOrder;
    }

    // Calculate total price of selected seats
    public long calculateTotalPrice(List<Integer> seatIds) throws Exception {
        long totalPrice = 0;
        for (int seatId : seatIds) {
            totalPrice += Math.round(seatRepo.getPrice(seatId));
        }
        return totalPrice;
    }

    // Invalidate pending orders that have not been updated for a certain period of time
    public void invalidatePendingOrders() throws Exception {
        // Get the current time
        LocalDateTime now = LocalDateTime.now();

        // Find all orders that are in a pending state and have not been updated for a certain period of time
        List<Order> orders = orderRepo.findByStatusAndUpdatedAtBefore(Order.PaymentStatus.PENDING, now.minusMinutes(15));

        for (Order order : orders) {

            // TODO: Query VNPay to check the status of the order

            // Update the order status to FAILED
            orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.FAILED);

            // Find the associated tickets and update their status to FAILED
            ticketRepo.updateStatusByOrderId(order.getOrderId(), Ticket.TicketStatus.FAILED.toInteger());
            List<Ticket> tickets = ticketRepo.findByOrderId(order.getOrderId());
            List<Integer> seatIds = tickets.stream().map(ticket -> ticket.getSeat().getSeatId()).toList();
            seatRepo.updateSeats(seatIds, Seat.TakenStatus.AVAILABLE);
        }
    }
}
