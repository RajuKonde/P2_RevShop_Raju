package com.revshop.dao.impl;

import com.revshop.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Category> categoryQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private CategoryDAOImpl categoryDAO;

    @Before
    public void setUp() {
        categoryDAO = new CategoryDAOImpl();
        ReflectionTestUtils.setField(categoryDAO, "em", entityManager);
    }

    @Test
    public void save_persistsNewCategoryAndReturnsSameInstance() {
        Category category = Category.builder()
                .name("Books")
                .active(true)
                .build();

        Category saved = categoryDAO.save(category);

        verify(entityManager).persist(category);
        assertSame(category, saved);
    }

    @Test
    public void save_mergesExistingCategory() {
        Category category = Category.builder()
                .id(8L)
                .name("Books")
                .active(true)
                .build();

        when(entityManager.merge(category)).thenReturn(category);

        Category saved = categoryDAO.save(category);

        verify(entityManager).merge(category);
        assertSame(category, saved);
    }

    @Test
    public void findBySlug_returnsFirstMatchingCategory() {
        Category category = Category.builder()
                .id(12L)
                .name("Home Decor")
                .slug("home-decor")
                .active(true)
                .build();

        when(entityManager.createQuery(anyString(), eq(Category.class))).thenReturn(categoryQuery);
        when(categoryQuery.setParameter("slug", "home-decor")).thenReturn(categoryQuery);
        when(categoryQuery.getResultStream()).thenReturn(Stream.of(category));

        Optional<Category> result = categoryDAO.findBySlug("home-decor");

        assertTrue(result.isPresent());
        assertSame(category, result.get());
    }

    @Test
    public void countActiveChildren_returnsZeroWhenQueryReturnsNull() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("parentId", 7L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(null);

        long count = categoryDAO.countActiveChildren(7L);

        assertTrue(count == 0L);
    }

    @Test
    public void findById_returnsEmptyWhenNoActiveCategoryMatches() {
        when(entityManager.createQuery(anyString(), eq(Category.class))).thenReturn(categoryQuery);
        when(categoryQuery.setParameter("id", 99L)).thenReturn(categoryQuery);
        when(categoryQuery.getResultStream()).thenReturn(Stream.empty());

        Optional<Category> result = categoryDAO.findById(99L);

        assertFalse(result.isPresent());
    }
}
