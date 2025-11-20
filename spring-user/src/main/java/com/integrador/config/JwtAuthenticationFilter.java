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
        
        // Solo loguear para endpoints de anuncios
        if (path.contains("/api/anuncios")) {
            System.out.println("=== FILTRO JWT ===");
            System.out.println("Path: " + path);
            System.out.println("Method: " + request.getMethod());
            System.out.println("Authorization header presente: " + (authHeader != null));
            if (authHeader != null) {
                System.out.println("Authorization header (primeros 50): " + authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
            }
            
            // Log de todos los headers
            System.out.println("Todos los headers:");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (headerName.toLowerCase().contains("auth") || headerName.toLowerCase().contains("content")) {
                    System.out.println("  " + headerName + ": " + request.getHeader(headerName));
                }
            });
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication en filtro: " + auth);
            System.out.println("Is authenticated: " + (auth != null && auth.isAuthenticated()));
            if (auth != null) {
                System.out.println("Principal class: " + auth.getPrincipal().getClass().getName());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

