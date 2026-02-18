package com.epam.aI_assisted_development.service;

import com.epam.aI_assisted_development.model.dto.OrderDto;
import com.epam.aI_assisted_development.model.entity.Order;
import com.epam.aI_assisted_development.model.entity.OrderStatus;
import com.epam.aI_assisted_development.repository.OrderRepository;
import com.epam.aI_assisted_development.specification.OrderSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final int DEFAULT_LIMIT = 10;
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderDto request) {
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .status(request.getStatus())
                .amount(request.getAmount())
                .build();
        return orderRepository.save(order);
    }

    public Page<Order> listOrders(
            Integer page,
            Integer limit,
            OrderStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant fromDate,
            Instant toDate
    ) {
        int normalizedPage = page == null ? 1 : page;
        int normalizedLimit = limit == null ? DEFAULT_LIMIT : limit;
        validatePagination(normalizedPage, normalizedLimit);
        validateRanges(minAmount, maxAmount, fromDate, toDate);

        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Order> spec = Specification.where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.amountGte(minAmount))
                .and(OrderSpecification.amountLte(maxAmount))
                .and(OrderSpecification.createdFrom(fromDate))
                .and(OrderSpecification.createdTo(toDate));

        return orderRepository.findAll(spec, pageable);
    }

    private void validatePagination(int page, int limit) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
    }

    private void validateRanges(BigDecimal minAmount, BigDecimal maxAmount, Instant fromDate, Instant toDate) {
        if (minAmount != null && maxAmount != null && maxAmount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("maxAmount must be >= minAmount");
        }
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be >= fromDate");
        }
    }
}
