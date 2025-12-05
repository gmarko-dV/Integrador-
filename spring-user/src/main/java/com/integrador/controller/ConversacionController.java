package com.integrador.controller;

import com.integrador.entity.Conversacion;
import com.integrador.entity.Mensaje;
import com.integrador.entity.Notificacion;
import com.integrador.service.ConversacionService;
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
@RequestMapping("/api/conversaciones")
@CrossOrigin(origins = "*")
public class ConversacionController {
    
    @Autowired
    private ConversacionService conversacionService;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Obtener el ID del usuario autenticado
     */
    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            return jwt.getClaimAsString("sub");
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return oauth2User.getAttribute("sub");
        }
        
        throw new IllegalArgumentException("No se pudo obtener el ID del usuario");
    }
    
    /**
     * Crear o obtener una conversación existente
     * POST /api/conversaciones
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearObtenerConversacion(
            @RequestBody Map<String, Object> request) {
        try {
            String userId = getUserId();
            Long idAnuncio = Long.valueOf(request.get("idAnuncio").toString());
            String idVendedor = request.get("idVendedor").toString();
            String idComprador = request.get("idComprador").toString();
            
            // Verificar que el usuario es parte de la conversación
            if (!userId.equals(idVendedor) && !userId.equals(idComprador)) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "No tienes permiso para crear esta conversación"));
            }
            
            // Verificar si ya existe una conversación antes de crear
            boolean esNuevaConversacion = !conversacionService.existeConversacion(
                idAnuncio, idVendedor, idComprador
            );
            
            System.out.println("=== CREAR/OBTENER CONVERSACIÓN ===");
            System.out.println("Es nueva conversación: " + esNuevaConversacion);
            System.out.println("User ID (quien hace la petición): " + userId);
            System.out.println("ID Vendedor: " + idVendedor);
            System.out.println("ID Comprador: " + idComprador);
            System.out.println("¿User es comprador? " + userId.equals(idComprador));
            System.out.println("¿User es vendedor? " + userId.equals(idVendedor));
            System.out.println("Request completo: " + request);
            
            Conversacion conversacion = conversacionService.crearObtenerConversacion(
                idAnuncio, idVendedor, idComprador
            );
            
            System.out.println("Conversación creada/obtenida con ID: " + conversacion.getIdConversacion());
            
            // Crear notificación SIEMPRE que el que contacta es el comprador (incluso si la conversación ya existe)
            // Esto asegura que el vendedor reciba notificación cada vez que alguien le contacta
            if (userId.equals(idComprador)) {
                System.out.println("✓ El usuario es el comprador, procediendo a crear notificación...");
                try {
                    System.out.println("=== INTENTANDO CREAR NOTIFICACIÓN ===");
                    System.out.println("Es nueva conversación: " + esNuevaConversacion);
                    
                    // Obtener nombre y email del comprador desde el token
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
                    
                    System.out.println("Nombre comprador: " + nombreComprador);
                    System.out.println("Email comprador: " + emailComprador);
                    
                    // Obtener mensaje del request
                    String mensajeNotificacion = null;
                    if (request.containsKey("mensaje") && request.get("mensaje") != null) {
                        Object mensajeObj = request.get("mensaje");
                        mensajeNotificacion = mensajeObj != null ? mensajeObj.toString() : null;
                    }
                    
                    if (mensajeNotificacion == null || mensajeNotificacion.trim().isEmpty()) {
                        mensajeNotificacion = "Un comprador está interesado en tu anuncio";
                    }
                    
                    System.out.println("Mensaje notificación: " + mensajeNotificacion);
                    System.out.println("Creando notificación para vendedor: " + idVendedor);
                    
                    Notificacion notificacion = notificacionService.crearNotificacion(
                        idVendedor, idComprador, idAnuncio, 
                        mensajeNotificacion, nombreComprador, emailComprador
                    );
                    
                    System.out.println("✓ Notificación creada exitosamente con ID: " + notificacion.getIdNotificacion());
                    System.out.println("  - Vendedor: " + notificacion.getIdVendedor());
                    System.out.println("  - Comprador: " + notificacion.getIdComprador());
                    System.out.println("  - Leida: " + notificacion.getLeida() + ", Leido: " + notificacion.getLeido());
                } catch (Exception e) {
                    // No fallar la creación de conversación si falla la notificación
                    System.err.println("=== ERROR AL CREAR NOTIFICACIÓN ===");
                    System.err.println("Mensaje: " + e.getMessage());
                    System.err.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
                    e.printStackTrace();
                }
            } else {
                System.out.println("No se crea notificación porque userId (" + userId + ") no es el comprador (" + idComprador + ")");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversacion", conversacion);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al crear/obtener conversación: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener todas las conversaciones del usuario autenticado
     * GET /api/conversaciones
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerConversaciones() {
        try {
            String userId = getUserId();
            List<Conversacion> conversaciones = conversacionService.obtenerConversacionesActivasPorUsuario(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversaciones", conversaciones);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener conversaciones: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener una conversación por ID
     * GET /api/conversaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerConversacion(@PathVariable Long id) {
        try {
            String userId = getUserId();
            Conversacion conversacion = conversacionService.obtenerConversacionPorId(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversacion", conversacion);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener conversación: " + e.getMessage()));
        }
    }
    
    /**
     * Enviar un mensaje
     * POST /api/conversaciones/{id}/mensajes
     */
    @PostMapping("/{id}/mensajes")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String userId = getUserId();
            String mensaje = request.get("mensaje");
            
            if (mensaje == null || mensaje.trim().isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "El mensaje no puede estar vacío"));
            }
            
            Mensaje nuevoMensaje = conversacionService.enviarMensaje(id, userId, mensaje);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", nuevoMensaje);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al enviar mensaje: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener mensajes de una conversación
     * GET /api/conversaciones/{id}/mensajes
     */
    @GetMapping("/{id}/mensajes")
    public ResponseEntity<Map<String, Object>> obtenerMensajes(@PathVariable Long id) {
        try {
            String userId = getUserId();
            List<Mensaje> mensajes = conversacionService.obtenerMensajesPorConversacion(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensajes", mensajes);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al obtener mensajes: " + e.getMessage()));
        }
    }
    
    /**
     * Marcar mensajes como leídos
     * PUT /api/conversaciones/{id}/mensajes/leer
     */
    @PutMapping("/{id}/mensajes/leer")
    public ResponseEntity<Map<String, Object>> marcarMensajesComoLeidos(@PathVariable Long id) {
        try {
            String userId = getUserId();
            conversacionService.marcarMensajesComoLeidos(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mensajes marcados como leídos");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al marcar mensajes como leídos: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener cantidad de mensajes no leídos
     * GET /api/conversaciones/mensajes/no-leidos
     */
    @GetMapping("/mensajes/no-leidos")
    public ResponseEntity<Map<String, Object>> contarMensajesNoLeidos() {
        try {
            String userId = getUserId();
            Long cantidad = conversacionService.contarMensajesNoLeidos(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cantidad", cantidad);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al contar mensajes no leídos: " + e.getMessage()));
        }
    }
    
    /**
     * Archivar una conversación
     * PUT /api/conversaciones/{id}/archivar
     */
    @PutMapping("/{id}/archivar")
    public ResponseEntity<Map<String, Object>> archivarConversacion(@PathVariable Long id) {
        try {
            String userId = getUserId();
            conversacionService.archivarConversacion(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conversación archivada");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al archivar conversación: " + e.getMessage()));
        }
    }
}

