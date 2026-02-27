package com.revshop.dao.impl;

import com.revshop.dao.ProductDAO;
import com.revshop.entity.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductDAOImpl implements ProductDAO {

    private final EntityManager em;

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
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    public List<Product> findBySellerEmail(String email) {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.seller.email = :email
                AND p.active = true
                """, Product.class)
                .setParameter("email", email)
                .getResultList();
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.category.id = :id
                AND p.active = true
                """, Product.class)
                .setParameter("id", categoryId)
                .getResultList();
    }

    @Override
    public List<Product> findActiveProducts() {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE p.active = true
                """, Product.class).getResultList();
    }

    @Override
    public List<Product> searchByName(String keyword) {
        return em.createQuery("""
                SELECT p FROM Product p
                WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :k, '%'))
                AND p.active = true
                """, Product.class)
                .setParameter("k", keyword)
                .getResultList();
    }
}