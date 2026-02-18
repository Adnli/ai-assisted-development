package com.epam.aI_assisted_development;

import com.epam.aI_assisted_development.model.entity.Order;
import com.epam.aI_assisted_development.model.entity.OrderStatus;
import com.epam.aI_assisted_development.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final int SEED_COUNT = 50;
    private static final long RANDOM_SEED = 424242L;
    private final OrderRepository orderRepository;

    public DataSeeder(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }
        Random random = new Random(RANDOM_SEED);
        List<Order> orders = new ArrayList<>(SEED_COUNT);
        Instant now = Instant.now();
        OrderStatus[] statuses = OrderStatus.values();

        for (int i = 0; i < SEED_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 500)
                    .setScale(2, RoundingMode.HALF_UP);
            Instant createdAt = now.minus(Duration.ofDays(random.nextInt(90)))
                    .minus(Duration.ofHours(random.nextInt(24)));
            Order order = Order.builder()
                    .customerName("Customer " + (i + 1))
                    .status(statuses[random.nextInt(statuses.length)])
                    .amount(amount)
                    .createdAt(createdAt)
                    .build();
            orders.add(order);
        }
        orderRepository.saveAll(orders);
    }
}
