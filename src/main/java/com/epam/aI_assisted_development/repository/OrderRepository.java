package com.epam.aI_assisted_development.repository;

import com.epam.aI_assisted_development.model.entity.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Spring Data JPA repository for Order.
// Extend JpaRepository and JpaSpecificationExecutor to support filtering specs.
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
}
