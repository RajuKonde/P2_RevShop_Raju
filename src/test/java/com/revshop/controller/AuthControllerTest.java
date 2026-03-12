package com.revshop.controller;

import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.dto.RegisterBuyerRequest;
import com.revshop.dto.UserResponse;
import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.password.ForgotPasswordRequest;
import com.revshop.dto.password.ForgotPasswordResponse;
import com.revshop.dto.password.ResetPasswordRequest;
import com.revshop.entity.Role;
import com.revshop.service.PasswordResetService;
import com.revshop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void registerBuyer_returnsWrappedSuccessResponse() {
        RegisterBuyerRequest request = new RegisterBuyerRequest();
        request.setEmail("buyer@test.com");
        request.setPassword("secret123");
        request.setFirstName("Raju");
        request.setLastName("Konde");

        UserResponse userResponse = UserResponse.builder()
                .id(11L)
                .email("buyer@test.com")
                .role(Role.BUYER)
                .active(true)
                .build();

        when(userService.registerBuyer(request)).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = authController.registerBuyer(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Buyer registered successfully", response.getBody().getMessage());
        assertEquals("buyer@test.com", response.getBody().getData().getEmail());
    }

    @Test
    public void login_returnsWrappedTokenResponse() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("seller@test.com");
        request.setPassword("secret123");

        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .token("jwt-token")
                .role("SELLER")
                .build();

        when(userService.login(request)).thenReturn(loginResponse);

        ResponseEntity<ApiResponse<LoginResponseDTO>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals("jwt-token", response.getBody().getData().getToken());
        assertEquals("SELLER", response.getBody().getData().getRole());
    }

    @Test
    public void forgotPassword_returnsGenericSuccessResponse() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("buyer@test.com");

        ForgotPasswordResponse forgotPasswordResponse = ForgotPasswordResponse.builder()
                .deliveryChannel("EMAIL")
                .expiresInMinutes(15)
                .note("If an account exists for this email, a password reset link has been sent.")
                .build();

        when(passwordResetService.generateResetToken("buyer@test.com")).thenReturn(forgotPasswordResponse);

        ResponseEntity<ApiResponse<ForgotPasswordResponse>> response = authController.forgotPassword(request);

        verify(passwordResetService).generateResetToken("buyer@test.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("If the account exists, a password reset link has been sent", response.getBody().getMessage());
        assertEquals("EMAIL", response.getBody().getData().getDeliveryChannel());
        assertEquals(Integer.valueOf(15), response.getBody().getData().getExpiresInMinutes());
    }

    @Test
    public void resetPassword_invokesServiceAndReturnsEmptyPayload() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("new-secret");

        ResponseEntity<ApiResponse<Void>> response = authController.resetPassword(request);

        verify(passwordResetService).resetPassword("reset-token", "new-secret");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password reset successful", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
