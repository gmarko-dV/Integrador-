package com.integrador.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtExceptionHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        System.out.println("=== ERROR DE AUTENTICACIÓN ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + request.getHeader("Authorization"));
        System.out.println("Error: " + authException.getMessage());
        System.out.println("Exception class: " + authException.getClass().getName());
        authException.printStackTrace();
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Error de autenticación: " + authException.getMessage() + "\"}");
    }
}

