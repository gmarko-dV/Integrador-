package com.integrador.controller;

import com.integrador.entity.Notificacion;
import com.integrador.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {
    
    @Autowired
    private NotificacionService notificacionService;
    
    private String obtenerUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            return jwt.getClaimAsString("sub");
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return oauth2User.getAttribute("sub");
        }
        return null;
    }
    
    @PostMapping("/contactar")
    public ResponseEntity<Map<String, Object>> contactarVendedor(
            @RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            String idComprador = obtenerUserId(authentication);
            if (idComprador == null || idComprador.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            // Obtener nombre y email del comprador desde el token
            String nombreComprador = null;
            String emailComprador = null;
            
            if (authentication instanceof JwtAuthenticationToken) {
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                nombreComprador = jwt.getClaimAsString("name");
                emailComprador = jwt.getClaimAsString("email");
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                nombreComprador = oauth2User.getAttribute("name");
                emailComprador = oauth2User.getAttribute("email");
            }
            
            String idVendedor = (String) request.get("idVendedor");
            Object idAnuncioObj = request.get("idAnuncio");
            String mensaje = (String) request.get("mensaje");
            
            if (idVendedor == null || idVendedor.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "ID del vendedor es requerido"));
            }
            
            if (idAnuncioObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "ID del anuncio es requerido"));
            }
            
            Long idAnuncio;
            try {
                idAnuncio = Long.parseLong(idAnuncioObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "ID del anuncio debe ser un número válido"));
            }
            
            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = "Un comprador está interesado en tu anuncio";
            }
            
            // Crear la notificación con información del comprador
            Notificacion notificacion = notificacionService.crearNotificacion(
                idVendedor, idComprador, idAnuncio, mensaje, nombreComprador, emailComprador);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación enviada exitosamente");
            response.put("notificacion", notificacion);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al crear la notificación: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerMisNotificaciones() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            String idVendedor = obtenerUserId(authentication);
            if (idVendedor == null || idVendedor.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesPorVendedor(idVendedor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notificaciones", notificaciones);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener las notificaciones: " + e.getMessage()));
        }
    }
    
    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Object>> obtenerNotificacionesNoLeidas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            String idVendedor = obtenerUserId(authentication);
            if (idVendedor == null || idVendedor.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            System.out.println("=== OBTENIENDO NOTIFICACIONES NO LEÍDAS (Controller) ===");
            System.out.println("ID Vendedor desde autenticación: " + idVendedor);
            
            List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesNoLeidasPorVendedor(idVendedor);
            Long cantidad = notificacionService.contarNotificacionesNoLeidas(idVendedor);
            
            System.out.println("Notificaciones retornadas: " + notificaciones.size());
            System.out.println("Cantidad no leídas: " + cantidad);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notificaciones", notificaciones);
            response.put("cantidadNoLeidas", cantidad);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR EN OBTENER NOTIFICACIONES NO LEÍDAS ===");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            
            // Retornar error más detallado
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener las notificaciones: " + e.getMessage());
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
            }
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<Map<String, Object>> marcarComoLeida(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            String idVendedor = obtenerUserId(authentication);
            if (idVendedor == null || idVendedor.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            Notificacion notificacion = notificacionService.marcarComoLeida(id, idVendedor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación marcada como leída");
            response.put("notificacion", notificacion);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al marcar la notificación: " + e.getMessage()));
        }
    }
    
    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Map<String, Object>> marcarTodasComoLeidas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            String idVendedor = obtenerUserId(authentication);
            if (idVendedor == null || idVendedor.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            notificacionService.marcarTodasComoLeidas(idVendedor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Todas las notificaciones han sido marcadas como leídas");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al marcar las notificaciones: " + e.getMessage()));
        }
    }
}

