package com.revshop.dto.order;

import com.revshop.entity.OrderStatus;
import com.revshop.entity.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private String orderNumber;
    private Long buyerId;
    private String buyerEmail;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String billingAddress;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
