package com.revshop.dao.impl;

import com.revshop.entity.Cart;
import com.revshop.entity.CartItem;
import com.revshop.entity.Notification;
import com.revshop.entity.Order;
import com.revshop.entity.OrderItem;
import com.revshop.entity.PasswordResetToken;
import com.revshop.entity.Payment;
import com.revshop.entity.Product;
import com.revshop.entity.ProductImage;
import com.revshop.entity.Review;
import com.revshop.entity.User;
import com.revshop.entity.WishlistItem;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RemainingDaoImplCoverageTest {

    @Test
    public void adminDAOImpl_saveUser_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        AdminDAOImpl dao = new AdminDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        User user = User.builder().email("admin@test.com").build();

        User saved = dao.saveUser(user);

        verify(entityManager).persist(user);
        assertSame(user, saved);
    }

    @Test
    public void cartDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        CartDAOImpl dao = new CartDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        Cart cart = Cart.builder().build();

        Cart saved = dao.save(cart);

        verify(entityManager).persist(cart);
        assertSame(cart, saved);
    }

    @Test
    public void cartItemDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        CartItemDAOImpl dao = new CartItemDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        CartItem item = CartItem.builder().build();

        CartItem saved = dao.save(item);

        verify(entityManager).persist(item);
        assertSame(item, saved);
    }

    @Test
    public void notificationDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        NotificationDAOImpl dao = new NotificationDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        Notification notification = Notification.builder().title("Hello").message("World").build();

        Notification saved = dao.save(notification);

        verify(entityManager).persist(notification);
        assertSame(notification, saved);
    }

    @Test
    public void orderDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        OrderDAOImpl dao = new OrderDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        Order order = Order.builder().orderNumber("ORD-100").build();

        Order saved = dao.save(order);

        verify(entityManager).persist(order);
        assertSame(order, saved);
    }

    @Test
    public void orderItemDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        OrderItemDAOImpl dao = new OrderItemDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        OrderItem orderItem = OrderItem.builder().quantity(1).build();

        OrderItem saved = dao.save(orderItem);

        verify(entityManager).persist(orderItem);
        assertSame(orderItem, saved);
    }

    @Test
    public void passwordResetTokenDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        PasswordResetTokenDAOImpl dao = new PasswordResetTokenDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        PasswordResetToken token = PasswordResetToken.builder().token("reset-token").build();

        PasswordResetToken saved = dao.save(token);

        verify(entityManager).persist(token);
        assertSame(token, saved);
    }

    @Test
    public void paymentDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        PaymentDAOImpl dao = new PaymentDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        Payment payment = Payment.builder().transactionRef("PAY-100").build();

        Payment saved = dao.save(payment);

        verify(entityManager).persist(payment);
        assertSame(payment, saved);
    }

    @Test
    public void productDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        ProductDAOImpl dao = new ProductDAOImpl(entityManager);
        Product product = Product.builder().name("Phone").build();

        Product saved = dao.save(product);

        verify(entityManager).persist(product);
        assertSame(product, saved);
    }

    @Test
    public void productImageDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        ProductImageDAOImpl dao = new ProductImageDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        ProductImage image = ProductImage.builder().imageUrl("/img.jpg").build();

        ProductImage saved = dao.save(image);

        verify(entityManager).persist(image);
        assertSame(image, saved);
    }

    @Test
    public void reviewDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        ReviewDAOImpl dao = new ReviewDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        Review review = Review.builder().title("Great").build();

        Review saved = dao.save(review);

        verify(entityManager).persist(review);
        assertSame(review, saved);
    }

    @Test
    public void wishlistDAOImpl_save_persistsNewEntity() {
        EntityManager entityManager = mock(EntityManager.class);
        WishlistDAOImpl dao = new WishlistDAOImpl();
        ReflectionTestUtils.setField(dao, "em", entityManager);
        WishlistItem item = WishlistItem.builder().build();

        WishlistItem saved = dao.save(item);

        verify(entityManager).persist(item);
        assertSame(item, saved);
    }
}
