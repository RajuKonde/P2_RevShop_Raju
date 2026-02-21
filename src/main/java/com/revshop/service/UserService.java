package com.revshop.service;

import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.entity.User;
//import com.revshop.security.jwt.JwtService;

public interface UserService {

    // ==============================
    // REGISTER BUYER
    // ==============================
    User registerBuyer(String email,
                       String rawPassword,
                       String firstName,
                       String lastName,
                       String phone,
                       String address);

    // ==============================
    // REGISTER SELLER
    // ==============================
    User registerSeller(String email,
                        String rawPassword,
                        String businessName,
                        String gstNumber,
                        String phone,
                        String businessAddress);

    // ==============================
    // LOGIN
    // ==============================
    LoginResponseDTO login(LoginRequestDTO request);

    // ==============================
    // FIND USER
    // ==============================
    User getByEmail(String email);
}