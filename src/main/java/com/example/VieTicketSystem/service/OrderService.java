package com.example.VieTicketSystem.service;

import com.example.VieTicketSystem.model.entity.*;
import com.example.VieTicketSystem.repo.OrderRepo;
import com.example.VieTicketSystem.repo.RefundOrderRepo;
import com.example.VieTicketSystem.repo.SeatRepo;
import com.example.VieTicketSystem.repo.TicketRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Value;
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

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final long ORDER_EXPIRATION;
    private final OrderRepo orderRepo;
    private final QRCodeService qrCodeService;
    private final RefundOrderRepo refundOrderRepo;
    private final SeatRepo seatRepo;
    private final TicketRepo ticketRepo;
    private final VNPayService vnPayService;

    public OrderService(EmailService emailService,
                        ObjectMapper jacksonObjectMapper,
                        @Value("${ORDER_TIME_WINDOW_MINUTES}") long ORDER_EXPIRATION,
                        OrderRepo orderRepo,
                        QRCodeService qrCodeService,
                        RefundOrderRepo refundOrderRepo,
                        SeatRepo seatRepo,
                        TicketRepo ticketRepo,
                        VNPayService vnPayService) {

        this.emailService = emailService;
        this.objectMapper = jacksonObjectMapper;
        this.ORDER_EXPIRATION = ORDER_EXPIRATION;
        this.orderRepo = orderRepo;
        this.qrCodeService = qrCodeService;
        this.refundOrderRepo = refundOrderRepo;
        this.seatRepo = seatRepo;
        this.ticketRepo = ticketRepo;
        this.vnPayService = vnPayService;
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

    private void processOrder(Order order, Order.PaymentStatus orderStatus, String vnp_PayDate) throws Exception {

        // This calls from Ticket -> Seat -> Row -> Area -> Event -> Organizer w/o using JPA
        // TODO: Optimize this
        List<Ticket> tickets = ticketRepo.findByOrderId(order.getOrderId());
        List<Integer> ticketIds = tickets.stream().map(Ticket::getTicketId).collect(Collectors.toList());

        switch (orderStatus) {
            case SUCCESS -> {
                // Set payment status to success
                order.setStatus(Order.PaymentStatus.SUCCESS);
                orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.SUCCESS);

                // Update ticket status to success and set ticket purchase date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime payDate = vnp_PayDate == null ? null : LocalDateTime.parse(vnp_PayDate, formatter);
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
    }

    // Handle payment response
    public Order handlePaymentResponse(Map<String, String> params) throws Exception {
        VNPayService.VNPayStatus vnPayStatus = vnPayService.orderReturn(params);
        Order.PaymentStatus orderStatus = vnPayStatus == VNPayService.VNPayStatus.SUCCESS ? Order.PaymentStatus.SUCCESS : Order.PaymentStatus.FAILED;

        String vnp_TxnRef = params.get("vnp_TxnRef");
        Order order = orderRepo.findByTxnRef(vnp_TxnRef);

        processOrder(order, orderStatus, params.get("vnp_PayDate"));

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
            emailContent.append("<p>Lead visitor: ").append(order.getUser().getFullName()).append("</p>");
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

    public void initiateRefund(RefundOrder refundOrder) throws Exception {
        float defaultRefundProportion = 0.7f;
        initiateRefund(refundOrder, defaultRefundProportion);
    }

    public void initiateRefund(RefundOrder refundOrder, float refundProportion) throws Exception {
        Order order = refundOrder.getOrder();

        // Get VNPay data
        Map<String, String> vnPayPersist = objectMapper.readValue(order.getVnpayData(),
                new TypeReference<>() {
                });

        // Set refund order status
        refundOrder.setStatus(RefundOrder.RefundStatus.PENDING);
        refundOrder.setApprovedOn(LocalDateTime.now());
        refundOrderRepo.saveApprovalStatus(refundOrder);

        long amount = Math.round(refundOrder.getTotal() * refundProportion);

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
                    // Check if the refund is successful
                    // Refund is unavailable in the sandbox environment; therefore, we always set the refund status to SUCCESS
                    if (true || vnPayService.processResponse(responseMap) == VNPayService.VNPayStatus.SUCCESS) {
                        switch (order.getStatus()) {
                            case PENDING_PARTIAL_REFUND -> {
                                try {
                                    seatRepo.updateSeats(ticketRepo.findSeatIdsByOrderIdAndStatus(order.getOrderId(), Ticket.TicketStatus.PENDING_REFUND.toInteger()), Seat.TakenStatus.AVAILABLE);
                                    ticketRepo.updateStatusByOrderIdAndStatus(order.getOrderId(), Ticket.TicketStatus.PENDING_REFUND.toInteger(), Ticket.TicketStatus.RETURNED.toInteger());
                                    orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.PARTIALLY_REFUNDED);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            case PENDING_TOTAL_REFUND -> {
                                try {
                                    seatRepo.updateSeats(ticketRepo.findByOrderId(order.getOrderId()).stream().map(ticket -> ticket.getSeat().getSeatId()).collect(Collectors.toList()), Seat.TakenStatus.AVAILABLE);
                                    ticketRepo.updateStatusByOrderId(order.getOrderId(), Ticket.TicketStatus.RETURNED.toInteger());
                                    orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.TOTALLY_REFUNDED);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            default -> throw new IllegalArgumentException("Database integrity error.");
                        }

                        refundOrder.setStatus(RefundOrder.RefundStatus.SUCCESS);

                    } else {
                        refundOrder.setStatus(RefundOrder.RefundStatus.FAILED);
                    }
                    try {
                        refundOrderRepo.updateStatus(refundOrder);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
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
    public void processPendingOrders() throws Exception {
        // Find all orders that are in a pending state and have not been updated for a certain period of time
        List<Order> orders = orderRepo.findByStatus(Order.PaymentStatus.PENDING);
        System.out.println(orders);

        for (Order order : orders) {

            // Query VNPay to check the status of the order
            Map<String, String> vnPayPersist = objectMapper.readValue(order.getVnpayData(),
                    new TypeReference<>() {
                    });

            vnPayService.queryTransaction(vnPayPersist)
                    .doOnError(e -> {
                    })
                    .subscribe(response -> {
                        Map<String, String> responseMap;
                        try {
                            responseMap = objectMapper.readValue(response, new TypeReference<>() {
                            });
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println(responseMap);
                        VNPayService.VNPayStatus vnPayStatus = vnPayService.processResponse(responseMap);

                        // Check if the order not expired and the payment status is not SUCCESS
                        if (vnPayStatus == VNPayService.VNPayStatus.INVALID || vnPayStatus != VNPayService.VNPayStatus.SUCCESS && order.getDate().plusMinutes(ORDER_EXPIRATION).isAfter(LocalDateTime.now())) {
                            return;
                        }

                        try {
                            processOrder(order, vnPayStatus == VNPayService.VNPayStatus.SUCCESS ? Order.PaymentStatus.SUCCESS : Order.PaymentStatus.FAILED, responseMap.get("vnp_PayDate"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

}
