package com.chada.service;

import com.chada.controller.WebSocketController;
import com.chada.dto.MonthlySalesDTO;
import com.chada.entity.Order;
import com.chada.entity.OrderItem;
import com.chada.entity.Product;
import com.chada.repository.OrderRepository;
import com.chada.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WebSocketController webSocketController;

    public Order createOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            // Only product id is sent in request
            Long productId = item.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));

            // Set product and order reference
            item.setProduct(product);
            item.setOrder(order);

            // Use price from request if provided (image-specific price), otherwise use product price
            // If product price is null (per-image pricing), item price must be provided
            BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : product.getPrice();
            if (itemPrice == null) {
                throw new RuntimeException("Price is required for product " + product.getId() + 
                        " (product uses per-image pricing, please specify price in order item)");
            }
            item.setPrice(itemPrice);

            // Add subtotal to total
            total = total.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Set total amount and default status
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PENDING);

        // Save order (items saved automatically via cascade)
        Order savedOrder = orderRepository.save(order);

        // Decrease product stock immediately when order is created
        decreaseProductStock(savedOrder);

        // Send email notification
        try {
            emailService.sendOrderNotification(savedOrder);
            logger.info("Email notification sent successfully for order #{}", savedOrder.getId());
        } catch (Exception e) {
            // Log error but don't fail the order creation
            logger.error("Failed to send email notification for order #{}: {}", savedOrder.getId(), e.getMessage(), e);
        }

        // Send SMS notification
        try {
            smsService.sendOrderNotification(savedOrder);
        } catch (Exception e) {
            // Log error but don't fail the order creation
            logger.error("Failed to send SMS notification for order #{}: {}", savedOrder.getId(), e.getMessage(), e);
        }

        // Broadcast WebSocket notification for real-time updates
        try {
            webSocketController.broadcastNewOrder(savedOrder);
            logger.debug("WebSocket notification broadcasted for order #{}", savedOrder.getId());
        } catch (Exception e) {
            // Log error but don't fail the order creation
            logger.error("Failed to broadcast WebSocket notification for order #{}: {}", savedOrder.getId(), e.getMessage(), e);
        }

        return savedOrder;
    }



    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        // Exclude DELIVERED orders from main list (they are archived)
        return orderRepository.findByStatusNotOrderByCreatedAtDesc(Order.OrderStatus.DELIVERED);
    }
    
    public List<Order> getArchivedOrders() {
        // Get only DELIVERED orders (archived)
        return orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.DELIVERED);
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
        
        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        
        // If changing to CANCELLED, restore product stock (stock was decreased when order was created)
        if (status == Order.OrderStatus.CANCELLED && previousStatus != Order.OrderStatus.CANCELLED) {
            restoreProductStock(order);
            // Clear delivery date if it was set
            if (order.getDeliveryDate() != null) {
                order.setDeliveryDate(null);
            }
        }
        // If changing from CANCELLED to another status, decrease stock again
        else if (previousStatus == Order.OrderStatus.CANCELLED && status != Order.OrderStatus.CANCELLED) {
            decreaseProductStock(order);
        }
        
        // If changing to DELIVERED, set delivery date
        if (status == Order.OrderStatus.DELIVERED && previousStatus != Order.OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }
        // If changing from DELIVERED to another status (except CANCELLED which is handled above), clear delivery date
        else if (previousStatus == Order.OrderStatus.DELIVERED && status != Order.OrderStatus.DELIVERED && status != Order.OrderStatus.CANCELLED) {
            order.setDeliveryDate(null);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * Decrease product stock when order is created or status changes from CANCELLED
     */
    private void decreaseProductStock(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                // Reload product to get latest stock
                Product latestProduct = productRepository.findById(product.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id " + product.getId()));
                
                // Handle per-image stock: if product stock is null, stock is managed per-image
                Integer currentStock = latestProduct.getStock();
                int quantityToDeduct = item.getQuantity();
                
                // If product uses per-image stock (stock is null), skip stock management
                // Stock is managed via imageDetails in the frontend
                if (currentStock == null) {
                    logger.debug("Product {} uses per-image stock management. Skipping product-level stock update.", latestProduct.getId());
                    continue;
                }
                
                // Check if sufficient stock is available
                if (currentStock < quantityToDeduct) {
                    throw new RuntimeException("Insufficient stock for product: " + latestProduct.getName() + 
                            ". Available: " + currentStock + ", Required: " + quantityToDeduct);
                }
                
                int newStock = currentStock - quantityToDeduct;
                latestProduct.setStock(newStock);
                productRepository.save(latestProduct);
                
                // Check if stock is low (less than 3) and send alert
                if (newStock < 3 && latestProduct.getActive()) {
                    try {
                        emailService.sendLowStockAlert(latestProduct);
                    } catch (Exception e) {
                        logger.error("Failed to send low stock alert email for product {}: {}", latestProduct.getId(), e.getMessage(), e);
                    }
                }
                
                // Log if stock went to zero
                if (newStock == 0) {
                    logger.warn("Product {} ({}) stock is now 0", latestProduct.getId(), latestProduct.getName());
                }
            }
        }
    }
    
    /**
     * Restore product stock when order is cancelled
     */
    private void restoreProductStock(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                // Reload product to get latest stock
                Product latestProduct = productRepository.findById(product.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id " + product.getId()));
                
                // Handle per-image stock: if product stock is null, stock is managed per-image
                Integer currentStock = latestProduct.getStock();
                
                // If product uses per-image stock (stock is null), skip stock management
                if (currentStock == null) {
                    logger.debug("Product {} uses per-image stock management. Skipping product-level stock restore.", latestProduct.getId());
                    continue;
                }
                
                int quantityToRestore = item.getQuantity();
                int newStock = currentStock + quantityToRestore;
                
                latestProduct.setStock(newStock);
                productRepository.save(latestProduct);
                
                logger.debug("Restored stock for product {} ({}): {} -> {}", latestProduct.getId(), latestProduct.getName(), currentStock, newStock);
            }
        }
    }

    public boolean deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) return false;
        
        // Before deleting, check if order is not CANCELLED, then restore stock
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && order.getStatus() != Order.OrderStatus.CANCELLED) {
            // Restore stock when deleting a non-cancelled order
            restoreProductStock(order);
        }
        
        orderRepository.deleteById(id);
        return true;
    }
    
    /**
     * Get weekly sales data for the last 7 days
     * Only includes DELIVERED orders (archived orders)
     */
    public List<MonthlySalesDTO> getMonthlySales() {
        List<Order> deliveredOrders = orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.DELIVERED);
        
        // Group orders by date (day)
        Map<String, List<Order>> ordersByDate = deliveredOrders.stream()
                .filter(order -> {
                    LocalDateTime orderDate = order.getDeliveryDate() != null 
                            ? order.getDeliveryDate() 
                            : order.getCreatedAt();
                    LocalDate date = orderDate.toLocalDate();
                    LocalDate today = LocalDate.now();
                    // Only include orders from the last 7 days
                    return !date.isBefore(today.minusDays(6));
                })
                .collect(Collectors.groupingBy(order -> {
                    LocalDateTime orderDate = order.getDeliveryDate() != null 
                            ? order.getDeliveryDate() 
                            : order.getCreatedAt();
                    return orderDate.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }));
        
        // Calculate sales for each day
        List<MonthlySalesDTO> weeklySales = new ArrayList<>();
        
        // Get last 7 days
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE dd/MM", Locale.FRENCH);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = date.format(dateFormatter);
            String dayName = date.format(dayFormatter);
            
            List<Order> dayOrders = ordersByDate.getOrDefault(dateKey, new ArrayList<>());
            BigDecimal totalSales = dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            weeklySales.add(new MonthlySalesDTO(dayName, dateKey, totalSales, dayOrders.size()));
        }
        
        return weeklySales;
    }
}
