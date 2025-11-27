package com.integrador.controller;

import com.integrador.dto.AnuncioRequest;
import com.integrador.entity.Anuncio;
import com.integrador.service.AnuncioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anuncios")
@CrossOrigin(origins = "*")
public class AnuncioController {
    
    @Autowired
    private AnuncioService anuncioService;
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> crearAnuncio(
            @RequestPart("modelo") String modelo,
            @RequestPart("anio") String anio,
            @RequestPart("kilometraje") String kilometraje,
            @RequestPart("precio") String precio,
            @RequestPart("descripcion") String descripcion,
            @RequestPart("tipoVehiculo") String tipoVehiculo,
            @RequestPart(value = "emailContacto", required = false) String emailContacto,
            @RequestPart(value = "telefonoContacto", required = false) String telefonoContacto,
            @RequestPart("imagen1") MultipartFile imagen1,
            @RequestPart("imagen2") MultipartFile imagen2) {
        
        try {
            System.out.println("=== INICIO CREAR ANUNCIO ===");
            
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + authentication);
            System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("ERROR: Usuario no autenticado");
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            // Obtener userId dependiendo del tipo de autenticación
            String userId = null;
            String email = null;
            if (authentication instanceof JwtAuthenticationToken) {
                // Autenticación con JWT token
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                userId = jwt.getClaimAsString("sub");
                email = jwt.getClaimAsString("email");
                System.out.println("User ID from JWT: " + userId);
                System.out.println("Email from JWT: " + email);
                
                // Validar dominio del email si está presente en el token
                if (email != null) {
                    String allowedDomain = "tecsup.edu.pe";
                    if (!email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())) {
                        System.out.println("ERROR: Email no permitido en JWT: " + email);
                        return ResponseEntity.status(403)
                            .body(Map.of("error", "Email no pertenece al dominio institucional permitido"));
                    }
                    System.out.println("Email validado correctamente en JWT");
                }
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                // Autenticación con OAuth2
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userId = oauth2User.getAttribute("sub");
                email = oauth2User.getAttribute("email");
                System.out.println("User ID from OAuth2: " + userId);
                System.out.println("Email from OAuth2: " + email);
            } else {
                System.out.println("ERROR: Tipo de autenticación no soportado: " + authentication.getClass());
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Tipo de autenticación no soportado"));
            }
            
            if (userId == null || userId.isEmpty()) {
                System.out.println("ERROR: No se pudo obtener el ID del usuario");
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            // Validar que las imágenes no estén vacías
            if (imagen1 == null || imagen1.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La primera imagen es requerida"));
            }
            if (imagen2 == null || imagen2.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La segunda imagen es requerida"));
            }
            
            // Crear el DTO
            AnuncioRequest request = new AnuncioRequest();
            request.setModelo(modelo);
            request.setAnio(Integer.parseInt(anio));
            request.setKilometraje(Integer.parseInt(kilometraje));
            request.setPrecio(new java.math.BigDecimal(precio));
            request.setDescripcion(descripcion);
            request.setTipoVehiculo(tipoVehiculo);
            request.setEmailContacto(emailContacto);
            request.setTelefonoContacto(telefonoContacto);
            
            System.out.println("Datos recibidos - Modelo: " + modelo + ", Año: " + anio);
            System.out.println("Email contacto: " + emailContacto);
            System.out.println("Teléfono contacto: " + telefonoContacto);
            
            // Preparar lista de imágenes
            List<MultipartFile> imagenes = List.of(imagen1, imagen2);
            
            // Crear el anuncio
            Anuncio anuncio = anuncioService.crearAnuncio(userId, request, imagenes);
            
            System.out.println("Anuncio creado exitosamente con ID: " + anuncio.getIdAnuncio());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Anuncio creado exitosamente");
            response.put("anuncio", anuncio);
            
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            System.out.println("ERROR NumberFormatException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error en el formato de los datos numéricos: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al crear el anuncio: " + e.getMessage()));
        }
    }
    
    @GetMapping("/mis-anuncios")
    public ResponseEntity<Map<String, Object>> obtenerMisAnuncios() {
        try {
            System.out.println("=== OBTENER MIS ANUNCIOS - INICIO ===");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication object: " + authentication);
            System.out.println("Authentication class: " + (authentication != null ? authentication.getClass().getName() : "null"));
            System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
            
            if (authentication == null) {
                System.out.println("ERROR: Authentication es null");
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            if (!authentication.isAuthenticated()) {
                System.out.println("ERROR: Authentication no está autenticado");
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            // Obtener userId dependiendo del tipo de autenticación
            String userId = null;
            if (authentication instanceof JwtAuthenticationToken) {
                // Autenticación con JWT token
                System.out.println("Tipo: JwtAuthenticationToken");
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                userId = jwt.getClaimAsString("sub");
                System.out.println("User ID del JWT: " + userId);
                System.out.println("JWT claims: " + jwt.getClaims());
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                // Autenticación con OAuth2
                System.out.println("Tipo: OAuth2User");
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userId = oauth2User.getAttribute("sub");
                System.out.println("User ID del OAuth2: " + userId);
            } else {
                System.out.println("ERROR: Tipo de autenticación no soportado: " + authentication.getClass().getName());
                System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Tipo de autenticación no soportado: " + authentication.getClass().getName()));
            }
            
            if (userId == null || userId.isEmpty()) {
                System.out.println("ERROR: No se pudo obtener el ID del usuario");
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            System.out.println("User ID final: " + userId);
            
            List<Anuncio> anuncios = anuncioService.obtenerAnunciosPorUsuario(userId);
            
            System.out.println("Anuncios encontrados: " + anuncios.size());
            anuncios.forEach(anuncio -> {
                System.out.println("Anuncio ID: " + anuncio.getIdAnuncio() + ", Usuario: " + anuncio.getIdUsuario() + ", Modelo: " + anuncio.getModelo());
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anuncios", anuncios);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("EXCEPCIÓN en obtenerMisAnuncios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener los anuncios: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodosLosAnuncios() {
        try {
            List<Anuncio> anuncios = anuncioService.obtenerTodosLosAnunciosActivos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anuncios", anuncios);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener los anuncios: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerAnuncioPorId(@PathVariable Long id) {
        try {
            Anuncio anuncio = anuncioService.obtenerAnuncioPorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("anuncio", anuncio);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener el anuncio: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarAnuncio(@PathVariable Long id) {
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            // Obtener userId dependiendo del tipo de autenticación
            String userId = null;
            if (authentication instanceof JwtAuthenticationToken) {
                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                userId = jwt.getClaimAsString("sub");
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userId = oauth2User.getAttribute("sub");
            }
            
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "No se pudo obtener el ID del usuario"));
            }
            
            // Eliminar el anuncio
            anuncioService.eliminarAnuncio(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Anuncio eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al eliminar el anuncio: " + e.getMessage()));
        }
    }
}

