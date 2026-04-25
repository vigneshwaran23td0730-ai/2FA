package com.auth.controller;

import com.auth.dto.RegisterRequest;
import com.auth.dto.LoginRequest;
import com.auth.entity.User;
import com.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("+1234567890");
        request.setPassword("SecurePass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    void register_ValidationError() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setName(""); // Invalid name
        request.setEmail("invalid-email"); // Invalid email
        request.setPassword("weak"); // Weak password

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void register_UserAlreadyExists() throws Exception {
        // Given
        User existingUser = User.builder()
                .name("Existing User")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .twoFaEnabled(false)
                .roles(Set.of(User.Role.USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("+1234567890");
        request.setPassword("SecurePass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("User already exists with email: john.doe@example.com"));
    }

    @Test
    void login_Success() throws Exception {
        // Given
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("SecurePass123!"))
                .enabled(true)
                .twoFaEnabled(false)
                .roles(Set.of(User.Role.USER))
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.requiresTwoFactor").value(false));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_With2FA_RequiresTwoFactor() throws Exception {
        // Given
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("SecurePass123!"))
                .enabled(true)
                .twoFaEnabled(true)
                .roles(Set.of(User.Role.USER))
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123!");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Login successful, 2FA required"))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.requiresTwoFactor").value(true));
    }
}