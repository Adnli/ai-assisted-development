package com.epam.aI_assisted_development.model.dto;

import com.epam.aI_assisted_development.model.entity.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// DTO for creating an order with validation:
// customerName not blank, status not null, amount not null and positive or zero.
// Use enum OrderStatus.
// No id/createdAt fields in request.
public class OrderDto {

    @NotBlank
    private String customerName;

    @NotNull
    private OrderStatus status;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal amount;
}
