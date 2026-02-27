package com.revshop.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessPaymentRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;

    // Mock switch to test failure path in Postman.
    private Boolean simulateFailure = false;
}
