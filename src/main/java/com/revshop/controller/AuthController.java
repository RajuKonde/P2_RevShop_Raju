package com.revshop.controller;

import com.revshop.dto.*;
import com.revshop.entity.User;
import com.revshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // REGISTER BUYER
    @PostMapping("/register/buyer")
    public ResponseEntity<UserResponse> registerBuyer(
            @Valid @RequestBody RegisterBuyerRequest request) {

        User user = userService.registerBuyer(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getAddress()
        );

        return ResponseEntity.ok(mapToResponse(user));
    }

    // REGISTER SELLER
    @PostMapping("/register/seller")
    public ResponseEntity<UserResponse> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request) {

        User user = userService.registerSeller(
                request.getEmail(),
                request.getPassword(),
                request.getBusinessName(),
                request.getGstNumber(),
                request.getPhone(),
                request.getBusinessAddress()
        );

        return ResponseEntity.ok(mapToResponse(user));
    }

    // ✅ LOGIN ENDPOINT (FIX)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(userService.login(request));
    }

    // MAPPER
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }
}