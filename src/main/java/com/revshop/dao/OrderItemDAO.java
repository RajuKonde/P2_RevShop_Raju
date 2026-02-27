package com.revshop.dao;

import com.revshop.entity.OrderItem;

import java.util.List;

public interface OrderItemDAO {

    OrderItem save(OrderItem orderItem);

    List<OrderItem> findByOrderId(Long orderId);
}
