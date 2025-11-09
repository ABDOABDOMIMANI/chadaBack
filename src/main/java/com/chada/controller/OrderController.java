package com.chada.controller;

import com.chada.dto.MonthlySalesDTO;
import com.chada.entity.Order;
import com.chada.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Créer une commande
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    // Récupérer une commande par ID
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Récupérer toutes les commandes (excluding archived/delivered)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    // Récupérer les commandes archivées (DELIVERED)
    @GetMapping(value = "/archived", produces = "application/json")
    public ResponseEntity<List<Order>> getArchivedOrders() {
        return ResponseEntity.ok(orderService.getArchivedOrders());
    }

    // Modifier le statut d’une commande
    @PutMapping(value = "/{id}/status", produces = "application/json")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status
    ) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // Supprimer une commande
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> deleteOrder(@PathVariable Long id) {
        boolean deleted = orderService.deleteOrder(id);
        return deleted ? ResponseEntity.ok(1) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
    }
    
    // Get monthly sales data
    @GetMapping(value = "/sales/monthly", produces = "application/json")
    public ResponseEntity<List<MonthlySalesDTO>> getMonthlySales() {
        return ResponseEntity.ok(orderService.getMonthlySales());
    }
}
