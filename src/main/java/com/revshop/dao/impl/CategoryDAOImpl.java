package com.revshop.dao.impl;

import com.revshop.dao.CategoryDAO;
import com.revshop.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDAOImpl implements CategoryDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            em.persist(category);
            return category;
        }
        return em.merge(category);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return em.createQuery("""
                SELECT c FROM Category c
                WHERE c.id = :id
                AND c.active = true
                AND c.isDeleted = false
                """, Category.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return em.createQuery("""
                SELECT c FROM Category c
                WHERE LOWER(c.name) = LOWER(:slug)
                AND c.active = true
                AND c.isDeleted = false
                """, Category.class)
                .setParameter("slug", slug)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Category> findAllActive() {
        return em.createQuery("""
                SELECT c FROM Category c
                WHERE c.active = true
                AND c.isDeleted = false
                ORDER BY c.name ASC
                """, Category.class)
                .getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        Long count = em.createQuery("""
                SELECT COUNT(c) FROM Category c
                WHERE LOWER(c.name) = LOWER(:name)
                AND c.isDeleted = false
                """, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count > 0;
    }
}
