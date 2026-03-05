package com.revshop.service.impl;

import com.revshop.dao.PasswordResetTokenDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.password.ForgotPasswordResponse;
import com.revshop.entity.PasswordResetToken;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private PasswordResetTokenDAO passwordResetTokenDAO;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @Test
    public void generateResetToken_deactivatesExistingTokensAndCreatesNewToken() {
        User user = User.builder()
                .id(7L)
                .email("buyer@test.com")
                .password("encoded-password")
                .role(Role.BUYER)
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenDAO.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeCall = LocalDateTime.now();

        ForgotPasswordResponse response = passwordResetService.generateResetToken("buyer@test.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenDAO).deactivateActiveTokensByUser(user);
        verify(passwordResetTokenDAO).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertTrue(savedToken.getActive());
        assertFalse(savedToken.getUsed());
        assertNotNull(savedToken.getToken());
        assertEquals(32, savedToken.getToken().length());

        assertEquals("buyer@test.com", response.getEmail());
        assertEquals(savedToken.getToken(), response.getResetToken());
        assertEquals(savedToken.getExpiresAt(), response.getExpiresAt());
        assertTrue(response.getExpiresAt().isAfter(beforeCall.plusMinutes(14)));
    }

    @Test
    public void resetPassword_updatesUserPasswordAndMarksTokenUsed() {
        User user = User.builder()
                .id(9L)
                .email("buyer@test.com")
                .password("old-password")
                .role(Role.BUYER)
                .active(true)
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token("active-token")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .active(true)
                .build();

        when(passwordResetTokenDAO.findActiveByToken("active-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new-secret");
        when(passwordResetTokenDAO.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.resetPassword("active-token", "new-secret");

        verify(userDAO).update(user);
        verify(passwordResetTokenDAO).save(resetToken);
        assertEquals("encoded-new-secret", user.getPassword());
        assertTrue(resetToken.getUsed());
        assertFalse(resetToken.getActive());
    }

    @Test
    public void resetPassword_throwsForExpiredTokenAndDisablesIt() {
        User user = User.builder()
                .id(10L)
                .email("buyer@test.com")
                .password("old-password")
                .role(Role.BUYER)
                .active(true)
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .active(true)
                .build();

        when(passwordResetTokenDAO.findActiveByToken("expired-token")).thenReturn(Optional.of(resetToken));
        when(passwordResetTokenDAO.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(BadRequestException.class,
                () -> passwordResetService.resetPassword("expired-token", "new-secret"));

        verify(passwordResetTokenDAO).save(resetToken);
        verify(userDAO, never()).update(any(User.class));
        assertFalse(resetToken.getActive());
        assertFalse(resetToken.getUsed());
    }
}
