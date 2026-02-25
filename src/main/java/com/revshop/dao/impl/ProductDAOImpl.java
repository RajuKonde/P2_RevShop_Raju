package com.revshop.dao.impl;

import com.revshop.dao.ProductDAO;
import com.revshop.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDAOImpl implements ProductDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            em.persist(product);
            return product;
        }
        return em.merge(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        Product product = em.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.seller.id = :sellerId
                AND p.deleted = false
                ORDER BY p.createdAt DESC
                """, Product.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.category.id = :categoryId
                AND p.deleted = false
                AND p.active = true
                """, Product.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    @Override
    public List<Product> findActiveProducts() {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.deleted = false AND p.active = true
                ORDER BY p.createdAt DESC
                """, Product.class)
                .getResultList();
    }

    @Override
    public void softDelete(Long id) {
        Product product = em.find(Product.class, id);
        if (product != null) {
            product.setDeleted(true);
            em.merge(product);
        }
    }
}