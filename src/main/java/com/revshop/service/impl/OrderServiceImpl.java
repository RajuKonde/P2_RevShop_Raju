package com.revshop.service.impl;

import com.revshop.dao.CartDAO;
import com.revshop.dao.CartItemDAO;
import com.revshop.dao.OrderDAO;
import com.revshop.dao.OrderItemDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.order.OrderItemResponse;
import com.revshop.dto.order.OrderResponse;
import com.revshop.dto.order.PlaceOrderRequest;
import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Order;
import com.revshop.entity.OrderItem;
import com.revshop.entity.OrderStatus;
import com.revshop.entity.Product;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ForbiddenOperationException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    @Override
    @Transactional
    public OrderResponse placeOrder(String buyerEmail, PlaceOrderRequest request) {
        User buyer = getValidatedBuyer(buyerEmail);
        Cart cart = cartDAO.findByBuyerId(buyer.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> cartItems = cartItemDAO.findActiveByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productDAO.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProduct().getId()));

            if (!Boolean.TRUE.equals(product.getActive()) || Boolean.TRUE.equals(product.getIsDeleted())) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }
            if (cartItem.getQuantity() > product.getStock()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .buyer(buyer)
                .status(OrderStatus.PLACED)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .totalAmount(totalAmount)
                .active(true)
                .build();
        orderDAO.save(order);

        for (CartItem cartItem : cartItems) {
            Product product = productDAO.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProduct().getId()));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .seller(product.getSeller())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .active(true)
                    .build();
            orderItemDAO.save(orderItem);

            int updatedStock = product.getStock() - cartItem.getQuantity();
            product.setStock(updatedStock);
            product.setInStock(updatedStock > 0);
            if (updatedStock <= 0) {
                product.setStatus(com.revshop.entity.ProductStatus.OUT_OF_STOCK);
            }
            productDAO.save(product);

            cartItem.setActive(false);
            cartItem.setIsDeleted(true);
            cartItemDAO.save(cartItem);
        }

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(String buyerEmail) {
        User buyer = getValidatedBuyer(buyerEmail);
        return orderDAO.findByBuyerId(buyer.getId())
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getBuyerOrderById(String buyerEmail, Long orderId) {
        User buyer = getValidatedBuyer(buyerEmail);
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new ForbiddenOperationException("Order does not belong to buyer");
        }
        if (!Boolean.TRUE.equals(order.getActive()) || Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new ResourceNotFoundException("Order not found");
        }

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(String sellerEmail) {
        User seller = getValidatedSeller(sellerEmail);
        return orderDAO.findBySellerEmail(seller.getEmail())
                .stream()
                .map(order -> buildOrderResponseForSeller(order, seller.getEmail()))
                .toList();
    }

    private User getValidatedBuyer(String email) {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.BUYER) {
            throw new ForbiddenOperationException("Only buyer can place/view buyer orders");
        }
        if (!Boolean.TRUE.equals(user.getActive()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ForbiddenOperationException("Buyer account is inactive");
        }
        return user;
    }

    private User getValidatedSeller(String email) {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.SELLER) {
            throw new ForbiddenOperationException("Only seller can view seller orders");
        }
        if (!Boolean.TRUE.equals(user.getActive()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ForbiddenOperationException("Seller account is inactive");
        }
        return user;
    }

    private OrderResponse buildOrderResponse(Order order) {
        List<OrderItemResponse> items = orderItemDAO.findByOrderId(order.getId())
                .stream()
                .map(this::mapOrderItem)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyer().getId())
                .buyerEmail(order.getBuyer().getEmail())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderResponse buildOrderResponseForSeller(Order order, String sellerEmail) {
        List<OrderItemResponse> items = orderItemDAO.findByOrderId(order.getId())
                .stream()
                .filter(item -> item.getSeller().getEmail().equals(sellerEmail))
                .map(this::mapOrderItem)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyer().getId())
                .buyerEmail(order.getBuyer().getEmail())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .totalAmount(items.stream().map(OrderItemResponse::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add))
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapOrderItem(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .sellerId(item.getSeller().getId())
                .sellerEmail(item.getSeller().getEmail())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    private String generateOrderNumber() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + token;
    }
}
