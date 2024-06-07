package com.example.VieTicketSystem.model.service;

import com.example.VieTicketSystem.model.entity.Order;
import com.example.VieTicketSystem.model.entity.Seat;
import com.example.VieTicketSystem.model.entity.Ticket;
import com.example.VieTicketSystem.model.entity.User;
import com.example.VieTicketSystem.model.repo.OrderRepo;
import com.example.VieTicketSystem.model.repo.SeatRepo;
import com.example.VieTicketSystem.model.repo.TicketRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final VNPayService vnPayService;
    private final ObjectMapper objectMapper;
    private final OrderRepo orderRepo;
    private final SeatRepo seatRepo;
    private final TicketRepo ticketRepo;

    public OrderService(VNPayService vnPayService, ObjectMapper jacksonObjectMapper, OrderRepo orderRepo, SeatRepo seatRepo, TicketRepo ticketRepo) {
        this.vnPayService = vnPayService;
        this.objectMapper = jacksonObjectMapper;
        this.orderRepo = orderRepo;
        this.seatRepo = seatRepo;
        this.ticketRepo = ticketRepo;
    }

    // Create order and return payment URL
    public String createOrder(List<Integer> selectedSeats, String orderInformation, String urlReturn, String clientIp, User user) throws Exception {

        long total = calculateTotalPrice(selectedSeats);
        Map<String, String> vnp_Params = new HashMap<>();
        String paymentURL = vnPayService.createOrder(total, orderInformation, urlReturn, clientIp, vnp_Params);

        // Persist vnpay data
        Map<String, String> vnpayPersist = new HashMap<>();
        vnpayPersist.put("vnp_TxnRef", vnp_Params.get("vnp_TxnRef"));
        vnpayPersist.put("vnp_Amount", vnp_Params.get("vnp_Amount"));
        vnpayPersist.put("vnp_OrderInfo", vnp_Params.get("vnp_OrderInfo"));
        vnpayPersist.put("vnp_CreateDate", vnp_Params.get("vnp_CreateDate"));

        // Save order to database
        Order order = new Order();
        order.setUser(user);
        order.setDate(LocalDateTime.now());
        order.setTotal(total);
        order.setStatus(Order.PaymentStatus.PENDING);
        order.setVnpayData(objectMapper.writeValueAsString(vnpayPersist));
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

                ticketRepo.setSuccessInBulk(ticketIds, payDate, Ticket.TicketStatus.PURCHASED.toInteger());

                // TODO: Send email to user
            }
            case FAILED -> {
                // Set payment status to failed
                order.setStatus(Order.PaymentStatus.FAILED);
                orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.FAILED);

                // Update ticket status to failed
                ticketRepo.setFailureInBulk(ticketIds, Ticket.TicketStatus.FAILED.toInteger());

                // Set seats to available
                seatRepo.updateSeats(tickets.stream().map(ticket -> ticket.getSeat().getSeatId()).collect(Collectors.toList()), false);
            }
        }

        return order;
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
            // Update the order status to FAILED
            orderRepo.updateStatus(order.getOrderId(), Order.PaymentStatus.FAILED);

            // Find the associated tickets and update their status to FAILED
            ticketRepo.updateStatusByOrderId(order.getOrderId(), Ticket.TicketStatus.FAILED.toInteger());
            List<Ticket> tickets = ticketRepo.findByOrderId(order.getOrderId());
            List<Integer> seatIds = tickets.stream().map(ticket -> ticket.getSeat().getSeatId()).toList();
            seatRepo.updateSeats(seatIds, false);
        }
    }
}
