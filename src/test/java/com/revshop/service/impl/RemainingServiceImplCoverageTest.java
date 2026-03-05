package com.revshop.service.impl;

import com.revshop.dao.AdminDAO;
import com.revshop.dao.CartDAO;
import com.revshop.dao.CartItemDAO;
import com.revshop.dao.CategoryDAO;
import com.revshop.dao.NotificationDAO;
import com.revshop.dao.OrderDAO;
import com.revshop.dao.OrderItemDAO;
import com.revshop.dao.PaymentDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.ProductImageDAO;
import com.revshop.dao.ReviewDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dao.WishlistDAO;
import com.revshop.dto.admin.AdminSummaryResponse;
import com.revshop.dto.cart.CartResponse;
import com.revshop.dto.order.OrderResponse;
import com.revshop.dto.payment.PaymentResponse;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.profile.ProfileResponse;
import com.revshop.dto.review.ProductRatingSummaryResponse;
import com.revshop.dto.seller.SellerDashboardResponse;
import com.revshop.dto.wishlist.WishlistStatusResponse;
import com.revshop.entity.BuyerProfile;
import com.revshop.entity.Cart;
import com.revshop.entity.Order;
import com.revshop.entity.OrderItem;
import com.revshop.entity.OrderStatus;
import com.revshop.entity.Payment;
import com.revshop.entity.PaymentMethod;
import com.revshop.entity.PaymentStatus;
import com.revshop.entity.Product;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.entity.WishlistItem;
import com.revshop.mapper.ProductMapper;
import com.revshop.service.NotificationService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemainingServiceImplCoverageTest {

    @Test
    public void adminServiceImpl_getSummary_returnsAggregateCounts() {
        AdminDAO adminDAO = mock(AdminDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        AdminServiceImpl service = new AdminServiceImpl(adminDAO, userDAO);
        ReflectionTestUtils.setField(service, "configuredAdminKey", "secret-key");

        User seller = activeUser(1L, "seller@test.com", Role.SELLER);
        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(adminDAO.countUsers(true)).thenReturn(20L);
        when(adminDAO.countUsersByRole(Role.BUYER, true)).thenReturn(12L);
        when(adminDAO.countUsersByRole(Role.SELLER, true)).thenReturn(8L);
        when(adminDAO.countUsersByActive(true, true)).thenReturn(18L);
        when(adminDAO.countDeletedUsers()).thenReturn(2L);
        when(adminDAO.countProducts()).thenReturn(50L);
        when(adminDAO.countOrders()).thenReturn(14L);
        when(adminDAO.countPayments()).thenReturn(10L);

        AdminSummaryResponse response = service.getSummary("seller@test.com", "secret-key");

        assertEquals(20L, response.getTotalUsers());
        assertEquals(12L, response.getTotalBuyers());
        assertEquals(8L, response.getTotalSellers());
        assertEquals(50L, response.getTotalProducts());
    }

    @Test
    public void cartServiceImpl_getCart_buildsCartResponseForBuyer() {
        CartDAO cartDAO = mock(CartDAO.class);
        CartItemDAO cartItemDAO = mock(CartItemDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        CartServiceImpl service = new CartServiceImpl(cartDAO, cartItemDAO, productDAO, userDAO);

        User buyer = activeUser(2L, "buyer@test.com", Role.BUYER);
        Cart cart = Cart.builder()
                .id(22L)
                .buyer(buyer)
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(cartDAO.findByBuyerId(2L)).thenReturn(Optional.of(cart));
        when(cartItemDAO.findActiveByCartId(22L)).thenReturn(List.of());

        CartResponse response = service.getCart("buyer@test.com");

        assertEquals(Long.valueOf(22L), response.getCartId());
        assertEquals(Integer.valueOf(0), response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getGrandTotal());
    }

    @Test
    public void notificationServiceImpl_getUnreadCount_returnsDaoCount() {
        NotificationDAO notificationDAO = mock(NotificationDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationDAO, userDAO);

        User user = activeUser(3L, "notify@test.com", Role.BUYER);
        when(userDAO.findByEmail("notify@test.com")).thenReturn(Optional.of(user));
        when(notificationDAO.countUnreadByRecipientEmail("notify@test.com")).thenReturn(6L);

        long unreadCount = service.getUnreadCount("notify@test.com");

        assertEquals(6L, unreadCount);
    }

    @Test
    public void orderServiceImpl_getBuyerOrderById_mapsOrderDetails() {
        OrderDAO orderDAO = mock(OrderDAO.class);
        OrderItemDAO orderItemDAO = mock(OrderItemDAO.class);
        CartDAO cartDAO = mock(CartDAO.class);
        CartItemDAO cartItemDAO = mock(CartItemDAO.class);
        PaymentDAO paymentDAO = mock(PaymentDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        NotificationService notificationService = mock(NotificationService.class);
        OrderServiceImpl service = new OrderServiceImpl(
                orderDAO,
                orderItemDAO,
                cartDAO,
                cartItemDAO,
                paymentDAO,
                productDAO,
                userDAO,
                notificationService
        );

        User buyer = activeUser(4L, "buyer@test.com", Role.BUYER);
        User seller = activeUser(5L, "seller@test.com", Role.SELLER);
        Product product = Product.builder()
                .id(44L)
                .name("Headphones")
                .seller(seller)
                .build();
        Order order = Order.builder()
                .id(101L)
                .orderNumber("ORD-101")
                .buyer(buyer)
                .status(OrderStatus.PLACED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .shippingAddress("Ship Street")
                .billingAddress("Bill Street")
                .totalAmount(new BigDecimal("799.00"))
                .active(true)
                .build();
        order.setIsDeleted(false);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem orderItem = OrderItem.builder()
                .id(1001L)
                .order(order)
                .product(product)
                .seller(seller)
                .quantity(1)
                .unitPrice(new BigDecimal("799.00"))
                .lineTotal(new BigDecimal("799.00"))
                .active(true)
                .build();

        Payment payment = Payment.builder()
                .id(201L)
                .order(order)
                .buyer(buyer)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("799.00"))
                .transactionRef("PAY-101")
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(orderDAO.findById(101L)).thenReturn(Optional.of(order));
        when(orderItemDAO.findByOrderId(101L)).thenReturn(List.of(orderItem));
        when(paymentDAO.findByOrderId(101L)).thenReturn(Optional.of(payment));

        OrderResponse response = service.getBuyerOrderById("buyer@test.com", 101L);

        assertEquals(Long.valueOf(101L), response.getOrderId());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(1, response.getItems().size());
        assertTrue(response.getCanCancel());
    }

    @Test
    public void paymentServiceImpl_getBuyerPayments_mapsStoredPayments() {
        PaymentDAO paymentDAO = mock(PaymentDAO.class);
        OrderDAO orderDAO = mock(OrderDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        PaymentServiceImpl service = new PaymentServiceImpl(paymentDAO, orderDAO, userDAO);

        User buyer = activeUser(6L, "buyer@test.com", Role.BUYER);
        Order order = Order.builder()
                .id(61L)
                .orderNumber("ORD-61")
                .buyer(buyer)
                .status(OrderStatus.CONFIRMED)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .totalAmount(new BigDecimal("1499.00"))
                .active(true)
                .build();
        Payment payment = Payment.builder()
                .id(62L)
                .order(order)
                .buyer(buyer)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("1499.00"))
                .transactionRef("PAY-61")
                .gatewayResponse("ok")
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(paymentDAO.findByBuyerId(6L)).thenReturn(List.of(payment));

        List<PaymentResponse> response = service.getBuyerPayments("buyer@test.com");

        assertEquals(1, response.size());
        assertEquals(Long.valueOf(62L), response.get(0).getPaymentId());
        assertEquals(OrderStatus.CONFIRMED, response.get(0).getOrderStatus());
    }

    @Test
    public void productServiceImpl_getAllActiveProducts_mapsResults() {
        ProductDAO productDAO = mock(ProductDAO.class);
        ProductImageDAO productImageDAO = mock(ProductImageDAO.class);
        CategoryDAO categoryDAO = mock(CategoryDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        ProductServiceImpl service = new ProductServiceImpl(productDAO, productImageDAO, categoryDAO, userDAO, productMapper);

        Product product = Product.builder()
                .id(7L)
                .name("Laptop")
                .build();
        ProductResponse productResponse = ProductResponse.builder()
                .id(7L)
                .name("Laptop")
                .build();

        when(productDAO.findActiveProducts()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        List<ProductResponse> response = service.getAllActiveProducts();

        assertEquals(1, response.size());
        assertEquals("Laptop", response.get(0).getName());
    }

    @Test
    public void profileServiceImpl_getMyProfile_normalizesPublicImageUrl() {
        UserDAO userDAO = mock(UserDAO.class);
        ProfileServiceImpl service = new ProfileServiceImpl(userDAO);

        User buyer = activeUser(8L, "profile@test.com", Role.BUYER);
        buyer.setProfileImageUrl("http://localhost:8080/uploads/profile-images/profile-8.jpg");
        buyer.setBuyerProfile(BuyerProfile.builder()
                .user(buyer)
                .firstName("Raju")
                .lastName("Konde")
                .phone("9999999999")
                .address("Hyderabad")
                .build());
        when(userDAO.findByEmail("profile@test.com")).thenReturn(Optional.of(buyer));

        ProfileResponse response = service.getMyProfile("profile@test.com");

        assertEquals(Long.valueOf(8L), response.getUserId());
        assertEquals("/uploads/profile-images/profile-8.jpg", response.getProfileImageUrl());
        assertEquals("Raju", response.getFirstName());
    }

    @Test
    public void reviewServiceImpl_getProductRatingSummary_aggregatesReviewMetrics() {
        ReviewDAO reviewDAO = mock(ReviewDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewDAO, userDAO, productDAO);

        Product product = Product.builder()
                .id(9L)
                .name("Phone")
                .active(true)
                .build();
        product.setIsDeleted(false);

        when(productDAO.findById(9L)).thenReturn(Optional.of(product));
        when(reviewDAO.countByProductId(9L)).thenReturn(3L);
        when(reviewDAO.averageRatingByProductId(9L)).thenReturn(new BigDecimal("4.50"));

        ProductRatingSummaryResponse response = service.getProductRatingSummary(9L);

        assertEquals(Long.valueOf(9L), response.getProductId());
        assertEquals(3L, response.getTotalReviews());
        assertEquals(new BigDecimal("4.50"), response.getAverageRating());
    }

    @Test
    public void sellerDashboardServiceImpl_getDashboard_buildsOverviewAndLists() {
        UserDAO userDAO = mock(UserDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        OrderItemDAO orderItemDAO = mock(OrderItemDAO.class);
        SellerDashboardServiceImpl service = new SellerDashboardServiceImpl(userDAO, productDAO, orderItemDAO);

        User seller = activeUser(10L, "seller@test.com", Role.SELLER);
        User buyer = activeUser(11L, "buyer@test.com", Role.BUYER);
        Order recentOrder = Order.builder()
                .id(301L)
                .orderNumber("ORD-301")
                .buyer(buyer)
                .status(OrderStatus.SHIPPED)
                .build();
        recentOrder.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));

        OrderItem firstItem = OrderItem.builder()
                .order(recentOrder)
                .seller(seller)
                .quantity(2)
                .lineTotal(new BigDecimal("200.00"))
                .build();
        OrderItem secondItem = OrderItem.builder()
                .order(recentOrder)
                .seller(seller)
                .quantity(1)
                .lineTotal(new BigDecimal("100.00"))
                .build();

        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(productDAO.countBySellerEmail("seller@test.com")).thenReturn(5L);
        when(productDAO.countActiveBySellerEmail("seller@test.com")).thenReturn(4L);
        when(productDAO.countLowStockBySellerEmail("seller@test.com", 3)).thenReturn(2L);
        when(orderItemDAO.countDistinctOrdersBySellerEmail("seller@test.com")).thenReturn(7L);
        when(orderItemDAO.countDistinctPendingOrdersBySellerEmail("seller@test.com")).thenReturn(2L);
        when(orderItemDAO.sumQuantityBySellerEmail("seller@test.com")).thenReturn(11L);
        when(orderItemDAO.sumRevenueBySellerEmail("seller@test.com")).thenReturn(new BigDecimal("999.50"));
        when(orderItemDAO.findBySellerEmail("seller@test.com")).thenReturn(List.of(firstItem, secondItem));
        when(orderItemDAO.findTopProductsBySellerEmail("seller@test.com", 1)).thenReturn(
                List.<Object[]>of(new Object[]{91L, "Laptop", 6, 8L, new BigDecimal("1500.00")})
        );

        SellerDashboardResponse response = service.getDashboard("seller@test.com", 2, 1, 3);

        assertNotNull(response.getOverview());
        assertEquals(Long.valueOf(5L), response.getOverview().getTotalProducts());
        assertEquals(1, response.getRecentOrders().size());
        assertEquals(Integer.valueOf(3), response.getRecentOrders().get(0).getItemCount());
        assertEquals(1, response.getTopProducts().size());
        assertEquals(Long.valueOf(91L), response.getTopProducts().get(0).getProductId());
    }

    @Test
    public void wishlistServiceImpl_getWishlistStatus_detectsActiveWishlistItem() {
        WishlistDAO wishlistDAO = mock(WishlistDAO.class);
        UserDAO userDAO = mock(UserDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        WishlistServiceImpl service = new WishlistServiceImpl(wishlistDAO, userDAO, productDAO);

        User buyer = activeUser(12L, "buyer@test.com", Role.BUYER);
        WishlistItem wishlistItem = WishlistItem.builder()
                .id(401L)
                .active(true)
                .build();
        wishlistItem.setIsDeleted(false);

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(wishlistDAO.findByBuyerIdAndProductId(12L, 45L)).thenReturn(Optional.of(wishlistItem));

        WishlistStatusResponse response = service.getWishlistStatus("buyer@test.com", 45L);

        assertEquals(Long.valueOf(45L), response.getProductId());
        assertTrue(response.getInWishlist());
    }

    private User activeUser(Long id, String email, Role role) {
        User user = User.builder()
                .id(id)
                .email(email)
                .role(role)
                .active(true)
                .build();
        user.setIsDeleted(false);
        return user;
    }
}
