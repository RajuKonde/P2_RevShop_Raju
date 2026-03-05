package com.revshop.dao.impl;

import com.revshop.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDAOImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> userQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private UserDAOImpl userDAO;

    @Before
    public void setUp() {
        userDAO = new UserDAOImpl();
        ReflectionTestUtils.setField(userDAO, "entityManager", entityManager);
    }

    @Test
    public void save_persistsAndReturnsSameUser() {
        User user = User.builder().email("buyer@test.com").build();

        User saved = userDAO.save(user);

        verify(entityManager).persist(user);
        assertSame(user, saved);
    }

    @Test
    public void findByEmail_returnsEmptyWhenNoResultExists() {
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("email", "missing@test.com")).thenReturn(userQuery);
        when(userQuery.getSingleResult()).thenThrow(new NoResultException());

        Optional<User> result = userDAO.findByEmail("missing@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    public void existsByEmail_returnsTrueWhenQueryCountIsPositive() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("email", "buyer@test.com")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        boolean exists = userDAO.existsByEmail("buyer@test.com");

        assertTrue(exists);
    }

    @Test
    public void delete_softDeletesExistingUser() {
        User user = User.builder()
                .id(22L)
                .email("buyer@test.com")
                .active(true)
                .build();

        when(entityManager.find(User.class, 22L)).thenReturn(user);

        userDAO.delete(22L);

        assertFalse(user.getActive());
        assertTrue(user.getIsDeleted());
        verify(entityManager).merge(user);
    }
}
