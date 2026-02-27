package com.revshop.dao.impl;

import com.revshop.dao.OrderItemDAO;
import com.revshop.entity.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDAOImpl implements OrderItemDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            em.persist(orderItem);
            return orderItem;
        }
        return em.merge(orderItem);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return em.createQuery("""
                SELECT oi FROM OrderItem oi
                WHERE oi.order.id = :orderId
                AND oi.active = true
                AND oi.isDeleted = false
                ORDER BY oi.createdAt ASC
                """, OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
