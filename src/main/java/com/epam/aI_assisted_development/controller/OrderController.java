package com.epam.aI_assisted_development.controller;

import com.epam.aI_assisted_development.model.dto.OrderDto;
import com.epam.aI_assisted_development.model.entity.Order;
import com.epam.aI_assisted_development.model.entity.OrderStatus;
import com.epam.aI_assisted_development.service.OrderService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDto request) {
        Order created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<OrderPageResponse> listOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate
    ) {
        Page<Order> result = orderService.listOrders(page, limit, status, minAmount, maxAmount, fromDate, toDate);
        OrderPageResponse response = new OrderPageResponse(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    public record OrderPageResponse(
            List<Order> items,
            int page,
            int limit,
            long totalItems,
            int totalPages
    ) {
    }
}
