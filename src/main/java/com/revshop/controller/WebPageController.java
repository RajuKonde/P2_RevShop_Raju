package com.revshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping({"/", "/home"})
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "auth/reset-password";
    }

    @GetMapping("/buyer/dashboard")
    public String buyerDashboardPage() {
        return "buyer/dashboard";
    }

    @GetMapping("/buyer/cart")
    public String buyerCartPage() {
        return "buyer/cart";
    }

    @GetMapping("/buyer/orders")
    public String buyerOrdersPage() {
        return "buyer/orders";
    }

    @GetMapping("/buyer/wishlist")
    public String buyerWishlistPage() {
        return "buyer/wishlist";
    }

    @GetMapping("/buyer/notifications")
    public String buyerNotificationsPage() {
        return "buyer/notifications";
    }

    @GetMapping("/buyer/profile")
    public String buyerProfilePage() {
        return "buyer/profile";
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboardPage() {
        return "seller/dashboard";
    }

    @GetMapping("/seller/products")
    public String sellerProductsPage() {
        return "seller/products";
    }

    @GetMapping("/seller/categories")
    public String sellerCategoriesPage() {
        return "seller/categories";
    }

    @GetMapping("/seller/notifications")
    public String sellerNotificationsPage() {
        return "seller/notifications";
    }

    @GetMapping("/seller/profile")
    public String sellerProfilePage() {
        return "seller/profile";
    }
}
