package com.example.fitness.user.interceptor;

import com.example.fitness.common.util.JwtUtil;
import com.example.fitness.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginInterceptor loginInterceptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSwaggerPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(loginInterceptor.preHandle(request, response, new Object()));

        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        assertTrue(loginInterceptor.preHandle(request, response, new Object()));

        when(request.getRequestURI()).thenReturn("/webjars/jquery.js");
        assertTrue(loginInterceptor.preHandle(request, response, new Object()));

        // Case insensitive check
        when(request.getRequestURI()).thenReturn("/V3/Api-Docs");
        assertTrue(loginInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    void testErrorPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/error");
        assertTrue(loginInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    void testValidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(jwtUtil.getUserIdFromToken("valid_token")).thenReturn("1");

        assertTrue(loginInterceptor.preHandle(request, response, new Object()));
        verify(request).setAttribute(eq("userId"), eq("1"));
    }

    @Test
    void testNoToken() {
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            loginInterceptor.preHandle(request, response, new Object());
        });
    }

    @Test
    void testInvalidToken() {
        when(request.getRequestURI()).thenReturn("/api/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        doThrow(new RuntimeException("invalid")).when(jwtUtil).validateToken("invalid_token");

        assertThrows(BusinessException.class, () -> {
            loginInterceptor.preHandle(request, response, new Object());
        });
    }
}
