package com.integrador.repository;

import com.integrador.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    
    // Buscar todos los mensajes de una conversación ordenados por fecha
    List<Mensaje> findByIdConversacionOrderByFechaEnvioAsc(Long idConversacion);
    
    // Marcar mensajes como leídos
    @Modifying
    @Transactional
    @Query("UPDATE Mensaje m SET m.leido = true " +
           "WHERE m.idConversacion = :idConversacion " +
           "AND m.idRemitente != :userId " +
           "AND m.leido = false")
    void marcarComoLeidos(@Param("idConversacion") Long idConversacion, @Param("userId") String userId);
    
    // Contar mensajes no leídos de un usuario en todas sus conversaciones
    @Query("SELECT COUNT(m) FROM Mensaje m " +
           "JOIN Conversacion c ON m.idConversacion = c.idConversacion " +
           "WHERE (c.idVendedor = :userId OR c.idComprador = :userId) " +
           "AND m.idRemitente != :userId " +
           "AND m.leido = false")
    Long countMensajesNoLeidosByUsuario(@Param("userId") String userId);
}

