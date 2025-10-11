package com.integrador.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", oauth2User.getAttribute("sub"));
            userInfo.put("name", oauth2User.getAttribute("name"));
            userInfo.put("email", oauth2User.getAttribute("email"));
            userInfo.put("picture", oauth2User.getAttribute("picture"));
            
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
    }

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> loginSuccess() {
        return ResponseEntity.ok(Map.of("message", "Login exitoso", "redirect", "/dashboard"));
    }

    @GetMapping("/failure")
    public ResponseEntity<Map<String, String>> loginFailure() {
        return ResponseEntity.status(401).body(Map.of("error", "Error en el login"));
    }

    @GetMapping("/logout-success")
    public ResponseEntity<Map<String, String>> logoutSuccess() {
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            response.put("user", Map.of(
                "id", oauth2User.getAttribute("sub"),
                "name", oauth2User.getAttribute("name"),
                "email", oauth2User.getAttribute("email")
            ));
        }
        
        return ResponseEntity.ok(response);
    }
}
