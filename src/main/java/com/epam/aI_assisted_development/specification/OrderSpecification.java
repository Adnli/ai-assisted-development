package com.epam.aI_assisted_development.specification;

import com.epam.aI_assisted_development.model.entity.Order;
import com.epam.aI_assisted_development.model.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

// Build JPA Specifications for filtering orders by:
// - status (exact match)
// - minAmount and maxAmount (BigDecimal range)
// - fromDate and toDate (Instant range inclusive)
// Ensure null-safe specs and no string concatenation SQL.
// Use CriteriaBuilder and root.get(...) properly.

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> amountGte(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Order> amountLte(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<Order> createdFrom(Instant fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<Order> createdTo(Instant toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
