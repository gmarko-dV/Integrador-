package com.integrador.service;

import com.integrador.entity.Anuncio;
import com.integrador.entity.Conversacion;
import com.integrador.entity.Mensaje;
import com.integrador.repository.AnuncioRepository;
import com.integrador.repository.ConversacionRepository;
import com.integrador.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversacionService {
    
    @Autowired
    private ConversacionRepository conversacionRepository;
    
    @Autowired
    private MensajeRepository mensajeRepository;
    
    @Autowired
    private AnuncioRepository anuncioRepository;
    
    /**
     * Verificar si existe una conversación entre vendedor y comprador sobre un anuncio
     */
    @Transactional(readOnly = true)
    public boolean existeConversacion(Long idAnuncio, String idVendedor, String idComprador) {
        return conversacionRepository
            .findByIdAnuncioAndIdVendedorAndIdCompradorWithMensajes(idAnuncio, idVendedor, idComprador)
            .isPresent();
    }
    
    /**
     * Crear o obtener una conversación existente entre vendedor y comprador sobre un anuncio
     */
    @Transactional
    public Conversacion crearObtenerConversacion(Long idAnuncio, String idVendedor, String idComprador) {
        // Verificar que el anuncio existe
        Anuncio anuncio = anuncioRepository.findById(idAnuncio)
            .orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
        
        // Verificar que el vendedor es el dueño del anuncio
        if (!anuncio.getIdUsuario().equals(idVendedor)) {
            throw new IllegalArgumentException("El usuario no es el vendedor de este anuncio");
        }
        
        // Verificar que el comprador no es el mismo que el vendedor
        if (idVendedor.equals(idComprador)) {
            throw new IllegalArgumentException("No puedes crear una conversación contigo mismo");
        }
        
        // Buscar conversación existente con mensajes cargados
        Optional<Conversacion> conversacionExistente = conversacionRepository
            .findByIdAnuncioAndIdVendedorAndIdCompradorWithMensajes(idAnuncio, idVendedor, idComprador);
        
        if (conversacionExistente.isPresent()) {
            Conversacion conv = conversacionExistente.get();
            // Si estaba inactiva, reactivarla
            if (!conv.getActiva()) {
                conv.setActiva(true);
                Conversacion convGuardada = conversacionRepository.save(conv);
                // Recargar con mensajes después de guardar
                return conversacionRepository.findByIdWithMensajes(convGuardada.getIdConversacion())
                    .orElse(convGuardada);
            }
            return conv;
        }
        
        // Crear nueva conversación
        Conversacion nuevaConversacion = new Conversacion(idAnuncio, idVendedor, idComprador);
        Conversacion convGuardada = conversacionRepository.save(nuevaConversacion);
        // Recargar con mensajes después de guardar
        return conversacionRepository.findByIdWithMensajes(convGuardada.getIdConversacion())
            .orElse(convGuardada);
    }
    
    /**
     * Obtener todas las conversaciones de un usuario
     */
    @Transactional(readOnly = true)
    public List<Conversacion> obtenerConversacionesPorUsuario(String userId) {
        return conversacionRepository.findByUsuario(userId);
    }
    
    /**
     * Obtener conversaciones activas de un usuario
     */
    @Transactional(readOnly = true)
    public List<Conversacion> obtenerConversacionesActivasPorUsuario(String userId) {
        return conversacionRepository.findActivasByUsuario(userId);
    }
    
    /**
     * Obtener una conversación por ID (con validación de acceso)
     */
    @Transactional(readOnly = true)
    public Conversacion obtenerConversacionPorId(Long idConversacion, String userId) {
        // Usar el método que carga los mensajes con JOIN FETCH para evitar problemas de lazy loading
        Conversacion conversacion = conversacionRepository.findByIdWithMensajes(idConversacion)
            .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));
        
        // Verificar que el usuario tiene acceso a esta conversación
        if (!conversacion.getIdVendedor().equals(userId) && 
            !conversacion.getIdComprador().equals(userId)) {
            throw new IllegalArgumentException("No tienes acceso a esta conversación");
        }
        
        return conversacion;
    }
    
    /**
     * Enviar un mensaje en una conversación
     */
    @Transactional
    public Mensaje enviarMensaje(Long idConversacion, String idRemitente, String mensaje) {
        // Obtener la conversación
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
            .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));
        
        // Verificar que el remitente es parte de la conversación
        if (!conversacion.getIdVendedor().equals(idRemitente) && 
            !conversacion.getIdComprador().equals(idRemitente)) {
            throw new IllegalArgumentException("No puedes enviar mensajes en esta conversación");
        }
        
        // Validar que el mensaje no esté vacío
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        // Crear y guardar el mensaje
        Mensaje nuevoMensaje = new Mensaje(idConversacion, idRemitente, mensaje.trim());
        nuevoMensaje = mensajeRepository.save(nuevoMensaje);
        
        // Actualizar fecha de último mensaje de la conversación
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);
        
        return nuevoMensaje;
    }
    
    /**
     * Obtener todos los mensajes de una conversación
     */
    @Transactional(readOnly = true)
    public List<Mensaje> obtenerMensajesPorConversacion(Long idConversacion, String userId) {
        // Verificar acceso
        obtenerConversacionPorId(idConversacion, userId);
        
        return mensajeRepository.findByIdConversacionOrderByFechaEnvioAsc(idConversacion);
    }
    
    /**
     * Marcar mensajes como leídos
     */
    @Transactional
    public void marcarMensajesComoLeidos(Long idConversacion, String userId) {
        // Verificar acceso
        obtenerConversacionPorId(idConversacion, userId);
        
        mensajeRepository.marcarComoLeidos(idConversacion, userId);
    }
    
    /**
     * Obtener cantidad de mensajes no leídos de un usuario
     */
    @Transactional(readOnly = true)
    public Long contarMensajesNoLeidos(String userId) {
        return mensajeRepository.countMensajesNoLeidosByUsuario(userId);
    }
    
    /**
     * Archivar/desactivar una conversación
     */
    @Transactional
    public void archivarConversacion(Long idConversacion, String userId) {
        Conversacion conversacion = obtenerConversacionPorId(idConversacion, userId);
        conversacion.setActiva(false);
        conversacionRepository.save(conversacion);
    }
}

