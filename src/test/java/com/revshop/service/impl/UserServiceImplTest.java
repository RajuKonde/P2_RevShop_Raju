package com.revshop.service.impl;

import com.revshop.dao.UserDAO;
import com.revshop.dto.LoginRequestDTO;
import com.revshop.dto.LoginResponseDTO;
import com.revshop.dto.RegisterBuyerRequest;
import com.revshop.dto.RegisterSellerRequest;
import com.revshop.dto.UserResponse;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ConflictException;
import com.revshop.mapper.AuthMapper;
import com.revshop.security.jwt.JwtService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void registerBuyer_encryptsPasswordAndPersistsBuyer() {
        RegisterBuyerRequest request = new RegisterBuyerRequest();
        request.setEmail("buyer@test.com");
        request.setPassword("secret123");
        request.setFirstName("Raju");
        request.setLastName("Konde");
        request.setPhone("9999999999");
        request.setAddress("Hyderabad");

        when(userDAO.existsByEmail("buyer@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(100L);
            return toSave;
        });
        when(authMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return UserResponse.builder()
                    .id(saved.getId())
                    .email(saved.getEmail())
                    .role(saved.getRole())
                    .active(saved.getActive())
                    .build();
        });

        UserResponse response = userService.registerBuyer(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).save(userCaptor.capture());
        User persisted = userCaptor.getValue();

        assertEquals(Role.BUYER, persisted.getRole());
        assertEquals("encoded-password", persisted.getPassword());
        assertNotNull(persisted.getBuyerProfile());
        assertEquals(persisted, persisted.getBuyerProfile().getUser());
        assertEquals("buyer@test.com", response.getEmail());
    }

    @Test
    public void registerSeller_throwsConflictWhenEmailAlreadyExists() {
        RegisterSellerRequest request = new RegisterSellerRequest();
        request.setEmail("seller@test.com");
        request.setPassword("secret123");
        request.setBusinessName("Rev Seller");

        when(userDAO.existsByEmail("seller@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.registerSeller(request));
    }

    @Test
    public void login_returnsTokenForValidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("seller@test.com");
        request.setPassword("secret123");

        User storedUser = User.builder()
                .email("seller@test.com")
                .password("encoded-password")
                .role(Role.SELLER)
                .active(true)
                .build();

        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("secret123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("seller@test.com", "SELLER")).thenReturn("jwt-token");

        LoginResponseDTO response = userService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("SELLER", response.getRole());
        verify(jwtService).generateToken(eq("seller@test.com"), eq("SELLER"));
    }

    @Test
    public void login_throwsBadRequestForInvalidPassword() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("buyer@test.com");
        request.setPassword("wrong-password");

        User storedUser = User.builder()
                .email("buyer@test.com")
                .password("encoded-password")
                .role(Role.BUYER)
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }
}
