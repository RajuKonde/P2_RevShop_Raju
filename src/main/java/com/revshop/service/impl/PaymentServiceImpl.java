package com.revshop.service.impl;

import com.revshop.dao.OrderDAO;
import com.revshop.dao.PaymentDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.payment.PaymentResponse;
import com.revshop.dto.payment.ProcessPaymentRequest;
import com.revshop.entity.Order;
import com.revshop.entity.OrderStatus;
import com.revshop.entity.Payment;
import com.revshop.entity.PaymentMethod;
import com.revshop.entity.PaymentStatus;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ConflictException;
import com.revshop.exception.ForbiddenOperationException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;

    @Override
    @Transactional
    public PaymentResponse processPayment(String buyerEmail, ProcessPaymentRequest request) {
        User buyer = getValidatedBuyer(buyerEmail);
        Order order = orderDAO.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateBuyerOwnership(order, buyer);
        validateOrderForPayment(order);

        Payment existing = paymentDAO.findByOrderId(order.getId()).orElse(null);
        if (existing != null && (existing.getStatus() == PaymentStatus.SUCCESS || existing.getStatus() == PaymentStatus.PENDING)) {
            throw new ConflictException("Payment already processed for this order");
        }

        PaymentStatus status = resolveMockStatus(order.getPaymentMethod(), request.getSimulateFailure());
        String transactionRef = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String gatewayResponse = buildGatewayResponse(order.getPaymentMethod(), status);

        Payment payment = existing == null ? Payment.builder()
                .order(order)
                .buyer(buyer)
                .paymentMethod(order.getPaymentMethod())
                .amount(order.getTotalAmount())
                .active(true)
                .build() : existing;

        payment.setStatus(status);
        payment.setTransactionRef(transactionRef);
        payment.setGatewayResponse(gatewayResponse);
        payment.setProcessedAt(LocalDateTime.now());
        paymentDAO.save(payment);

        if (status == PaymentStatus.SUCCESS || status == PaymentStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else if (status == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.PLACED);
        }
        orderDAO.save(order);

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getBuyerPayments(String buyerEmail) {
        User buyer = getValidatedBuyer(buyerEmail);
        return paymentDAO.findByBuyerId(buyer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(String buyerEmail, Long orderId) {
        User buyer = getValidatedBuyer(buyerEmail);
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validateBuyerOwnership(order, buyer);

        Payment payment = paymentDAO.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
        return mapToResponse(payment);
    }

    private User getValidatedBuyer(String email) {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.BUYER) {
            throw new ForbiddenOperationException("Only buyer can access payment APIs");
        }
        if (!Boolean.TRUE.equals(user.getActive()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ForbiddenOperationException("Buyer account is inactive");
        }
        return user;
    }

    private void validateBuyerOwnership(Order order, User buyer) {
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new ForbiddenOperationException("Order does not belong to buyer");
        }
    }

    private void validateOrderForPayment(Order order) {
        if (!Boolean.TRUE.equals(order.getActive()) || Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new ResourceNotFoundException("Order not found");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Payment cannot be processed for this order status");
        }
    }

    private PaymentStatus resolveMockStatus(PaymentMethod method, Boolean simulateFailure) {
        boolean fail = Boolean.TRUE.equals(simulateFailure);
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return PaymentStatus.PENDING;
        }
        return fail ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;
    }

    private String buildGatewayResponse(PaymentMethod method, PaymentStatus status) {
        if (method == PaymentMethod.CASH_ON_DELIVERY) {
            return "Mock COD selected. Payment pending until delivery.";
        }
        if (status == PaymentStatus.SUCCESS) {
            return "Mock payment authorized successfully.";
        }
        return "Mock gateway declined transaction.";
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .buyerId(payment.getBuyer().getId())
                .buyerEmail(payment.getBuyer().getEmail())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getStatus())
                .orderStatus(payment.getOrder().getStatus())
                .amount(payment.getAmount())
                .transactionRef(payment.getTransactionRef())
                .gatewayResponse(payment.getGatewayResponse())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}
