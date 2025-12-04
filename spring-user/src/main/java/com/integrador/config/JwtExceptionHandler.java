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
        
        System.out.println("\n❌ ========== ERROR DE AUTENTICACIÓN ==========");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            System.out.println("Authorization header presente: SÍ");
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                System.out.println("Token (primeros 50 chars): " + token.substring(0, Math.min(50, token.length())) + "...");
                System.out.println("Token completo length: " + token.length());
            } else {
                System.out.println("Authorization header no empieza con 'Bearer '");
            }
        } else {
            System.out.println("Authorization header presente: NO");
        }
        
        System.out.println("Error: " + authException.getMessage());
        System.out.println("Exception class: " + authException.getClass().getName());
        
        // Log de la causa raíz si existe
        Throwable cause = authException.getCause();
        if (cause != null) {
            System.out.println("Causa: " + cause.getClass().getName() + " - " + cause.getMessage());
            System.out.println("Causa completa:");
            cause.printStackTrace();
            
            // Si es un error de JWT, intentar decodificar el token para ver qué algoritmo usa
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Decodificar el header del JWT para ver el algoritmo
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
                        System.out.println("🔍 JWT Header decodificado: " + header);
                        
                        // Intentar decodificar el payload también
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        System.out.println("🔍 JWT Payload decodificado (primeros 200 chars): " + payload.substring(0, Math.min(200, payload.length())));
                    }
                } catch (Exception e) {
                    System.out.println("No se pudo decodificar el JWT para diagnóstico: " + e.getMessage());
                }
            }
        } else {
            authException.printStackTrace();
        }
        
        System.out.println("==========================================\n");
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Error de autenticación: " + authException.getMessage() + "\"}");
    }
}

