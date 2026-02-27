package com.revshop.service.impl;

import com.revshop.dao.UserDAO;
import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.dto.RegisterBuyerRequest;
import com.revshop.dto.RegisterSellerRequest;
import com.revshop.dto.UserResponse;
import com.revshop.entity.*;
import com.revshop.mapper.AuthMapper;
import com.revshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.revshop.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    // ==============================
    // REGISTER BUYER
    // ==============================
    @Override
    @Transactional
    public UserResponse registerBuyer(RegisterBuyerRequest request) {

        validateEmailUniqueness(request.getEmail());

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encryptedPassword)
                .role(Role.BUYER)
                .active(true)
                .build();

        BuyerProfile profile = BuyerProfile.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .user(user)
                .build();

        user.setBuyerProfile(profile);

        return authMapper.toUserResponse(userDAO.save(user));
    }

    // ==============================
    // REGISTER SELLER
    // ==============================
    @Override
    @Transactional
    public UserResponse registerSeller(RegisterSellerRequest request) {

        validateEmailUniqueness(request.getEmail());

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encryptedPassword)
                .role(Role.SELLER)
                .active(true)
                .build();

        SellerProfile profile = SellerProfile.builder()
                .businessName(request.getBusinessName())
                .gstNumber(request.getGstNumber())
                .phone(request.getPhone())
                .businessAddress(request.getBusinessAddress())
                .user(user)
                .build();

        user.setSellerProfile(profile);

        return authMapper.toUserResponse(userDAO.save(user));
    }

    // ==============================
    // LOGIN
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User is disabled");
        }

        // ✅ GENERATE JWT
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponseDTO.builder()
                .token(token)
                .role(user.getRole().name())
                .build();
    }
    // ==============================
    // FIND USER BY EMAIL
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return authMapper.toUserResponse(user);
    }

    // ==============================
    // PRIVATE HELPERS
    // ==============================
    private void validateEmailUniqueness(String email) {
        if (userDAO.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }
    }
}
