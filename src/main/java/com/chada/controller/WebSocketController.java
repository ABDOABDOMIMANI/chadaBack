package com.chada.controller;

import com.chada.entity.Order;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast new order notification to all connected clients
     */
    public void broadcastNewOrder(Order order) {
        OrderNotification notification = new OrderNotification(
            order.getId(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getCustomerPhone(),
            order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0,
            order.getCreatedAt() != null ? order.getCreatedAt().toString() : java.time.LocalDateTime.now().toString()
        );
        
        // Send to /topic/orders - all subscribers will receive this
        messagingTemplate.convertAndSend("/topic/orders", notification);
        
        System.out.println("WebSocket: Broadcasted new order notification for order #" + order.getId());
    }

    /**
     * Handle client messages (optional - for testing)
     */
    @MessageMapping("/orders")
    @SendTo("/topic/orders")
    public OrderNotification handleOrderMessage(OrderNotification notification) {
        return notification;
    }

    /**
     * DTO for order notifications
     */
    public static class OrderNotification {
        private Long orderId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private Double totalAmount;
        private String createdAt;

        public OrderNotification() {
        }

        public OrderNotification(Long orderId, String customerName, String customerEmail, 
                               String customerPhone, Double totalAmount, String createdAt) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.customerPhone = customerPhone;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}

