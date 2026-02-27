package com.revshop.service;

import com.revshop.dto.order.OrderResponse;
import com.revshop.dto.order.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(String buyerEmail, PlaceOrderRequest request);

    List<OrderResponse> getBuyerOrders(String buyerEmail);

    OrderResponse getBuyerOrderById(String buyerEmail, Long orderId);

    List<OrderResponse> getSellerOrders(String sellerEmail);
}
