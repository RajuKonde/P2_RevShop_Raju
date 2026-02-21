package com.revshop.service.impl;

import com.revshop.dao.UserDAO;
import com.revshop.entity.*;
import com.revshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    private final BCryptPasswordEncoder passwordEncoder;
    // ==============================
    // REGISTER BUYER
    // ==============================
    @Override
    @Transactional
    public User registerBuyer(String email,
                              String rawPassword,
                              String firstName,
                              String lastName,
                              String phone,
                              String address) {

        validateEmailUniqueness(email);

        String encryptedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .email(email)
                .password(encryptedPassword)
                .role(Role.BUYER)
                .active(true)
                .build();

        BuyerProfile profile = BuyerProfile.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .address(address)
                .user(user)
                .build();

        user.setBuyerProfile(profile);

        return userDAO.save(user);
    }

    // ==============================
    // REGISTER SELLER
    // ==============================
    @Override
    @Transactional
    public User registerSeller(String email,
                               String rawPassword,
                               String businessName,
                               String gstNumber,
                               String phone,
                               String businessAddress) {

        validateEmailUniqueness(email);

        String encryptedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .email(email)
                .password(encryptedPassword)
                .role(Role.SELLER)
                .active(true)
                .build();

        SellerProfile profile = SellerProfile.builder()
                .businessName(businessName)
                .gstNumber(gstNumber)
                .phone(phone)
                .businessAddress(businessAddress)
                .user(user)
                .build();

        user.setSellerProfile(profile);

        return userDAO.save(user);
    }

    // ==============================
    // FIND USER BY EMAIL
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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