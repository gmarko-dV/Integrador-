package com.integrador.controller;

import com.integrador.entity.HistorialBusqueda;
import com.integrador.entity.Vehiculo;
import com.integrador.service.PlateSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlateSearchController {
    
    @Autowired
    private PlateSearchService plateSearchService;
    
    @PostMapping("/plate-search")
    public ResponseEntity<Map<String, Object>> searchPlate(@RequestBody Map<String, String> request) {
        try {
            String plateNumber = request.get("plateNumber");
            String userId = request.get("userId");
            
            if (plateNumber == null || plateNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número de placa es requerido"));
            }
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El ID de usuario es requerido"));
            }
            
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            Map<String, Object> result = plateSearchService.searchPlate(plateNumber.toUpperCase(), userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Búsqueda realizada exitosamente");
            response.put("vehicle", result);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }
    
    @GetMapping("/plate-search/history")
    public ResponseEntity<Map<String, Object>> getSearchHistory() {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String userId = oauth2User.getAttribute("sub");
            
            List<HistorialBusqueda> history = plateSearchService.getSearchHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener el historial: " + e.getMessage()));
        }
    }
    
    @GetMapping("/plate-search/recent")
    public ResponseEntity<Map<String, Object>> getRecentVehicles() {
        try {
            List<Vehiculo> recentVehicles = plateSearchService.getRecentVehicles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vehicles", recentVehicles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener vehículos recientes: " + e.getMessage()));
        }
    }
    
    @GetMapping("/plate-search/validate/{plate}")
    public ResponseEntity<Map<String, Object>> validatePlate(@PathVariable String plate) {
        try {
            boolean isValid = plate != null && plate.matches("^[A-Z]{3}[0-9]{3,4}$");
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("plate", plate.toUpperCase());
            
            if (!isValid) {
                response.put("message", "Formato de placa inválido. Debe ser 3 letras seguidas de 3-4 números (ej: ABC123)");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al validar la placa: " + e.getMessage()));
        }
    }

    @GetMapping("/plate-search/test")
    public String test() {
        return "Spring Boot funcionando - Servicio SOAP Perú integrado";
    }

    // Endpoint para ver el JSON crudo
    @GetMapping("/plate-search/raw/{placa}")
    public ResponseEntity<String> searchRaw(@PathVariable String placa) {
        try {
            // Usar el servicio directamente para obtener JSON crudo
            String jsonResponse = plateSearchService.getRawApiResponse(placa);
            return ResponseEntity.ok("JSON crudo de la API:\n" + jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
