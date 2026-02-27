package com.revshop.controller;

import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.dto.RegisterBuyerRequest;
import com.revshop.dto.RegisterSellerRequest;
import com.revshop.dto.UserResponse;
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
public class AuthController {

    private final UserService userService;

    @PostMapping("/register/buyer")
    public ResponseEntity<UserResponse> registerBuyer(
            @Valid @RequestBody RegisterBuyerRequest request) {
        return ResponseEntity.ok(userService.registerBuyer(request));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<UserResponse> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request) {
        return ResponseEntity.ok(userService.registerSeller(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
