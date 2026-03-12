package com.revshop.service;

public interface EmailService {

    void sendPasswordResetEmail(String recipientEmail, String resetLink, int expiryMinutes);
}
