package com.hell.backend.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hell.backend.users.dto.LoginRequest;
import com.hell.backend.users.dto.SignUpRequest;
import com.hell.backend.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 테스트")
    @WithMockUser
    public void testSignup() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        logger.info("회원가입 요청 생성: {}", request);

        when(userService.save(any(SignUpRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        logger.info("회원가입 성공적으로 테스트 완료");

        verify(userService, times(1)).save(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    @WithMockUser
    public void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        String mockToken = "mockJwtToken";
        when(userService.login(any(LoginRequest.class))).thenReturn(mockToken);

        logger.info("로그인 요청 생성: {}", request);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken))
                .andExpect(content().string("Login successful"));

        logger.info("로그인 성공적으로 테스트 완료");

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    @WithMockUser
    public void testLogout() throws Exception {
        logger.info("로그아웃 요청 시작");

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk());

        logger.info("로그아웃 성공적으로 테스트 완료");
    }
}
