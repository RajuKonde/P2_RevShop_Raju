package com.revshop.controller;

import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.dto.RegisterBuyerRequest;
import com.revshop.dto.RegisterSellerRequest;
import com.revshop.dto.UserResponse;
import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.password.ForgotPasswordRequest;
import com.revshop.dto.password.ForgotPasswordResponse;
import com.revshop.dto.password.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.revshop.service.PasswordResetService;
import com.revshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and registration APIs")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register/buyer")
    public ResponseEntity<ApiResponse<UserResponse>> registerBuyer(
            @Valid @RequestBody RegisterBuyerRequest request) {
        UserResponse userResponse = userService.registerBuyer(request);
        return ResponseEntity.ok(ApiResponse.success("Buyer registered successfully", userResponse));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<UserResponse>> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request) {
        UserResponse userResponse = userService.registerSeller(request);
        return ResponseEntity.ok(ApiResponse.success("Seller registered successfully", userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        ForgotPasswordResponse response = passwordResetService.generateResetToken(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset token generated", response));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }
}
