package com.revshop.service;

import com.revshop.entity.User;

public interface UserService {

    User registerBuyer(String email,
                       String rawPassword,
                       String firstName,
                       String lastName,
                       String phone,
                       String address);

    User registerSeller(String email,
                        String rawPassword,
                        String businessName,
                        String gstNumber,
                        String phone,
                        String businessAddress);

    User getByEmail(String email);
}