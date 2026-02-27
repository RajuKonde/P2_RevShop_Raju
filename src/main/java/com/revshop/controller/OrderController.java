package com.revshop.controller;

import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.order.OrderResponse;
import com.revshop.dto.order.PlaceOrderRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.revshop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and order history APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @Valid @RequestBody PlaceOrderRequest request,
            Authentication auth
    ) {
        OrderResponse response = orderService.placeOrder(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders(Authentication auth) {
        List<OrderResponse> response = orderService.getBuyerOrders(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Buyer orders fetched", response));
    }

    @GetMapping("/my/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> myOrderById(
            @PathVariable Long orderId,
            Authentication auth
    ) {
        OrderResponse response = orderService.getBuyerOrderById(auth.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Buyer order fetched", response));
    }

    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> sellerOrders(Authentication auth) {
        List<OrderResponse> response = orderService.getSellerOrders(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Seller orders fetched", response));
    }
}
