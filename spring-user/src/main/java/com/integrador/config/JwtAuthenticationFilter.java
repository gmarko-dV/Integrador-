package com.integrador.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();
        
        // Log detallado para debugging (solo en desarrollo)
        if (path.contains("/api/") && (path.contains("/anuncios") || path.contains("/notificaciones"))) {
            System.out.println("=== FILTRO JWT ===");
            System.out.println("Path: " + path);
            System.out.println("Method: " + request.getMethod());
            System.out.println("Authorization header presente: " + (authHeader != null));
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                System.out.println("Token (primeros 50 chars): " + token.substring(0, Math.min(50, token.length())) + "...");
            }
        }
        
        // Continuar con la cadena de filtros (Spring Security OAuth2 Resource Server procesará el token)
        filterChain.doFilter(request, response);
        
        // Log después de procesar (para ver si la autenticación fue exitosa)
        if (path.contains("/api/") && (path.contains("/anuncios") || path.contains("/notificaciones"))) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication después del filtro: " + (auth != null ? auth.getClass().getSimpleName() : "null"));
            System.out.println("Is authenticated: " + (auth != null && auth.isAuthenticated()));
            if (auth != null && auth.isAuthenticated()) {
                System.out.println("✅ Usuario autenticado correctamente");
            } else {
                System.out.println("❌ Usuario NO autenticado");
            }
        }
    }
}

