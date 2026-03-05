package com.revshop.controller;

import com.revshop.dto.admin.AdminSummaryResponse;
import com.revshop.dto.cart.CartResponse;
import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.notification.UnreadCountResponse;
import com.revshop.dto.order.OrderResponse;
import com.revshop.dto.payment.PaymentResponse;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.profile.ProfileResponse;
import com.revshop.dto.review.ProductRatingSummaryResponse;
import com.revshop.dto.seller.SellerDashboardOverviewResponse;
import com.revshop.dto.seller.SellerDashboardResponse;
import com.revshop.dto.wishlist.WishlistStatusResponse;
import com.revshop.entity.Role;
import com.revshop.service.AdminService;
import com.revshop.service.CartService;
import com.revshop.service.NotificationService;
import com.revshop.service.OrderService;
import com.revshop.service.PaymentService;
import com.revshop.service.ProductService;
import com.revshop.service.ProfileService;
import com.revshop.service.ReviewService;
import com.revshop.service.SellerDashboardService;
import com.revshop.service.WishlistService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemainingControllerCoverageTest {

    @Test
    public void adminController_summary_returnsWrappedSuccessResponse() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService);
        Authentication authentication = authentication("seller@test.com");

        AdminSummaryResponse summary = AdminSummaryResponse.builder()
                .totalUsers(12)
                .totalSellers(4)
                .build();
        when(adminService.getSummary("seller@test.com", "secret-key")).thenReturn(summary);

        ResponseEntity<ApiResponse<AdminSummaryResponse>> response = controller.summary(authentication, "secret-key");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Admin summary fetched", response.getBody().getMessage());
        assertEquals(12L, response.getBody().getData().getTotalUsers());
        verify(adminService).getSummary("seller@test.com", "secret-key");
    }

    @Test
    public void cartController_getCart_returnsWrappedSuccessResponse() {
        CartService cartService = mock(CartService.class);
        CartController controller = new CartController(cartService);
        Authentication authentication = authentication("buyer@test.com");

        CartResponse cartResponse = CartResponse.builder()
                .cartId(21L)
                .buyerEmail("buyer@test.com")
                .totalItems(2)
                .grandTotal(new BigDecimal("499.00"))
                .build();
        when(cartService.getCart("buyer@test.com")).thenReturn(cartResponse);

        ResponseEntity<ApiResponse<CartResponse>> response = controller.getCart(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cart fetched successfully", response.getBody().getMessage());
        assertEquals(Integer.valueOf(2), response.getBody().getData().getTotalItems());
        verify(cartService).getCart("buyer@test.com");
    }

    @Test
    public void notificationController_unreadCount_returnsWrappedCountResponse() {
        NotificationService notificationService = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notificationService);
        Authentication authentication = authentication("buyer@test.com");

        when(notificationService.getUnreadCount("buyer@test.com")).thenReturn(4L);

        ResponseEntity<ApiResponse<UnreadCountResponse>> response = controller.unreadCount(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unread notification count fetched", response.getBody().getMessage());
        assertEquals(Long.valueOf(4L), response.getBody().getData().getUnreadCount());
        verify(notificationService).getUnreadCount("buyer@test.com");
    }

    @Test
    public void orderController_myOrders_returnsWrappedSuccessResponse() {
        OrderService orderService = mock(OrderService.class);
        OrderController controller = new OrderController(orderService);
        Authentication authentication = authentication("buyer@test.com");

        List<OrderResponse> orders = List.of(
                OrderResponse.builder()
                        .orderId(33L)
                        .orderNumber("ORD-33")
                        .build()
        );
        when(orderService.getBuyerOrders("buyer@test.com")).thenReturn(orders);

        ResponseEntity<ApiResponse<List<OrderResponse>>> response = controller.myOrders(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Buyer orders fetched", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(orderService).getBuyerOrders("buyer@test.com");
    }

    @Test
    public void paymentController_myPayments_returnsWrappedSuccessResponse() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentService);
        Authentication authentication = authentication("buyer@test.com");

        List<PaymentResponse> payments = List.of(
                PaymentResponse.builder()
                        .paymentId(41L)
                        .orderNumber("ORD-41")
                        .build()
        );
        when(paymentService.getBuyerPayments("buyer@test.com")).thenReturn(payments);

        ResponseEntity<ApiResponse<List<PaymentResponse>>> response = controller.myPayments(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Buyer payments fetched", response.getBody().getMessage());
        assertEquals(Long.valueOf(41L), response.getBody().getData().get(0).getPaymentId());
        verify(paymentService).getBuyerPayments("buyer@test.com");
    }

    @Test
    public void productController_publicProducts_returnsWrappedSuccessResponse() {
        ProductService productService = mock(ProductService.class);
        ProductController controller = new ProductController(productService);

        List<ProductResponse> products = List.of(
                ProductResponse.builder()
                        .id(51L)
                        .name("Laptop")
                        .build()
        );
        when(productService.getAllActiveProducts()).thenReturn(products);

        ResponseEntity<ApiResponse<List<ProductResponse>>> response = controller.publicProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Active products fetched", response.getBody().getMessage());
        assertEquals("Laptop", response.getBody().getData().get(0).getName());
        verify(productService).getAllActiveProducts();
    }

    @Test
    public void profileController_myProfile_returnsWrappedSuccessResponse() {
        ProfileService profileService = mock(ProfileService.class);
        ProfileController controller = new ProfileController(profileService);
        Authentication authentication = authentication("buyer@test.com");

        ProfileResponse profileResponse = ProfileResponse.builder()
                .userId(61L)
                .email("buyer@test.com")
                .role(Role.BUYER)
                .build();
        when(profileService.getMyProfile("buyer@test.com")).thenReturn(profileResponse);

        ResponseEntity<ApiResponse<ProfileResponse>> response = controller.myProfile(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Profile fetched", response.getBody().getMessage());
        assertEquals(Role.BUYER, response.getBody().getData().getRole());
        verify(profileService).getMyProfile("buyer@test.com");
    }

    @Test
    public void reviewController_productRatingSummary_returnsWrappedSuccessResponse() {
        ReviewService reviewService = mock(ReviewService.class);
        ReviewController controller = new ReviewController(reviewService);

        ProductRatingSummaryResponse summary = ProductRatingSummaryResponse.builder()
                .productId(71L)
                .productName("Phone")
                .totalReviews(8)
                .averageRating(new BigDecimal("4.60"))
                .build();
        when(reviewService.getProductRatingSummary(71L)).thenReturn(summary);

        ResponseEntity<ApiResponse<ProductRatingSummaryResponse>> response = controller.productRatingSummary(71L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Product rating summary fetched", response.getBody().getMessage());
        assertEquals(8L, response.getBody().getData().getTotalReviews());
        verify(reviewService).getProductRatingSummary(71L);
    }

    @Test
    public void sellerDashboardController_dashboard_returnsWrappedSuccessResponse() {
        SellerDashboardService sellerDashboardService = mock(SellerDashboardService.class);
        SellerDashboardController controller = new SellerDashboardController(sellerDashboardService);
        Authentication authentication = authentication("seller@test.com");

        SellerDashboardResponse dashboardResponse = SellerDashboardResponse.builder()
                .overview(SellerDashboardOverviewResponse.builder()
                        .totalProducts(9L)
                        .build())
                .build();
        when(sellerDashboardService.getDashboard("seller@test.com", 5, 5, 3)).thenReturn(dashboardResponse);

        ResponseEntity<ApiResponse<SellerDashboardResponse>> response = controller.dashboard(authentication, 5, 5, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Seller dashboard fetched", response.getBody().getMessage());
        assertEquals(Long.valueOf(9L), response.getBody().getData().getOverview().getTotalProducts());
        verify(sellerDashboardService).getDashboard("seller@test.com", 5, 5, 3);
    }

    @Test
    public void testController_secureEndpoint_returnsSuccessPayload() {
        TestController controller = new TestController();

        ResponseEntity<ApiResponse<String>> response = controller.secureEndpoint();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("JWT verification successful", response.getBody().getMessage());
        assertEquals("JWT WORKING SUCCESSFULLY", response.getBody().getData());
    }

    @Test
    public void webPageController_returnsExpectedViewNames() {
        WebPageController controller = new WebPageController();

        assertEquals("index", controller.homePage());
        assertEquals("auth/login", controller.loginPage());
        assertEquals("seller/profile", controller.sellerProfilePage());
    }

    @Test
    public void wishlistController_status_returnsWrappedSuccessResponse() {
        WishlistService wishlistService = mock(WishlistService.class);
        WishlistController controller = new WishlistController(wishlistService);
        Authentication authentication = authentication("buyer@test.com");

        WishlistStatusResponse statusResponse = WishlistStatusResponse.builder()
                .productId(91L)
                .inWishlist(true)
                .build();
        when(wishlistService.getWishlistStatus("buyer@test.com", 91L)).thenReturn(statusResponse);

        ResponseEntity<ApiResponse<WishlistStatusResponse>> response = controller.wishlistStatus(91L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Wishlist status fetched", response.getBody().getMessage());
        assertTrue(response.getBody().getData().getInWishlist());
        verify(wishlistService).getWishlistStatus("buyer@test.com", 91L);
    }

    private Authentication authentication(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }
}
