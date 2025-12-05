package com.integrador.repository;

import com.integrador.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    
    // Buscar conversación existente entre vendedor y comprador sobre un anuncio
    Optional<Conversacion> findByIdAnuncioAndIdVendedorAndIdComprador(
        Long idAnuncio, String idVendedor, String idComprador
    );
    
    // Buscar conversación existente con mensajes cargados
    @Query("SELECT DISTINCT c FROM Conversacion c LEFT JOIN FETCH c.mensajes " +
           "WHERE c.idAnuncio = :idAnuncio AND c.idVendedor = :idVendedor AND c.idComprador = :idComprador")
    Optional<Conversacion> findByIdAnuncioAndIdVendedorAndIdCompradorWithMensajes(
        @Param("idAnuncio") Long idAnuncio, 
        @Param("idVendedor") String idVendedor, 
        @Param("idComprador") String idComprador
    );
    
    // Buscar todas las conversaciones de un usuario (como vendedor o comprador)
    @Query("SELECT DISTINCT c FROM Conversacion c LEFT JOIN FETCH c.mensajes " +
           "WHERE c.idVendedor = :userId OR c.idComprador = :userId " +
           "ORDER BY c.fechaUltimoMensaje DESC NULLS LAST, c.fechaCreacion DESC")
    List<Conversacion> findByUsuario(@Param("userId") String userId);
    
    // Buscar conversaciones activas de un usuario
    @Query("SELECT DISTINCT c FROM Conversacion c LEFT JOIN FETCH c.mensajes " +
           "WHERE (c.idVendedor = :userId OR c.idComprador = :userId) AND c.activa = true " +
           "ORDER BY c.fechaUltimoMensaje DESC NULLS LAST")
    List<Conversacion> findActivasByUsuario(@Param("userId") String userId);
    
    // Buscar conversaciones de un anuncio específico
    List<Conversacion> findByIdAnuncioOrderByFechaUltimoMensajeDesc(Long idAnuncio);
    
    // Buscar conversación por ID con mensajes cargados
    @Query("SELECT DISTINCT c FROM Conversacion c LEFT JOIN FETCH c.mensajes WHERE c.idConversacion = :id")
    Optional<Conversacion> findByIdWithMensajes(@Param("id") Long id);
    
    // Contar mensajes no leídos de un usuario en una conversación
    @Query("SELECT COUNT(m) FROM Mensaje m " +
           "WHERE m.idConversacion = :idConversacion " +
           "AND m.idRemitente != :userId " +
           "AND m.leido = false")
    Long countMensajesNoLeidos(@Param("idConversacion") Long idConversacion, @Param("userId") String userId);
}

