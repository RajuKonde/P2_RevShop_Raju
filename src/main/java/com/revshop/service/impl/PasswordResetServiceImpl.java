package com.revshop.service.impl;

import com.revshop.dao.PasswordResetTokenDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.password.ForgotPasswordResponse;
import com.revshop.entity.PasswordResetToken;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.service.EmailService;
import com.revshop.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final String GENERIC_FORGOT_PASSWORD_NOTE =
            "If an account exists for this email, a password reset link has been sent.";

    private final UserDAO userDAO;
    private final PasswordResetTokenDAO passwordResetTokenDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.base-url:http://localhost:8080/reset-password}")
    private String passwordResetBaseUrl = "http://localhost:8080/reset-password";

    @Override
    @Transactional
    public ForgotPasswordResponse generateResetToken(String email) {
        Optional<User> userOptional = userDAO.findByEmail(email);
        if (userOptional.isEmpty() || !Boolean.TRUE.equals(userOptional.get().getActive())) {
            log.info("Password reset requested for unavailable account: {}", email);
            return buildForgotPasswordResponse();
        }

        User user = userOptional.get();

        passwordResetTokenDAO.deactivateActiveTokensByUser(user);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
        String token = UUID.randomUUID().toString().replace("-", "");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .active(true)
                .build();
        passwordResetTokenDAO.save(resetToken);

        String resetLink = UriComponentsBuilder.fromUriString(passwordResetBaseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink, TOKEN_EXPIRY_MINUTES);
        return buildForgotPasswordResponse();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenDAO.findActiveByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or inactive reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setActive(false);
            passwordResetTokenDAO.save(resetToken);
            throw new BadRequestException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userDAO.update(user);

        resetToken.setUsed(true);
        resetToken.setActive(false);
        passwordResetTokenDAO.save(resetToken);
    }

    private ForgotPasswordResponse buildForgotPasswordResponse() {
        return ForgotPasswordResponse.builder()
                .deliveryChannel("EMAIL")
                .expiresInMinutes(TOKEN_EXPIRY_MINUTES)
                .note(GENERIC_FORGOT_PASSWORD_NOTE)
                .build();
    }
}
