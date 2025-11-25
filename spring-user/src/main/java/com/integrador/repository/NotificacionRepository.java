package com.integrador.repository;

import com.integrador.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByIdVendedorOrderByFechaCreacionDesc(String idVendedor);
    
    List<Notificacion> findByIdVendedorAndLeidaFalseOrderByFechaCreacionDesc(String idVendedor);
    
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.idVendedor = :idVendedor AND (n.leida = false OR n.leida IS NULL)")
    Long countNoLeidasByIdVendedor(@Param("idVendedor") String idVendedor);
    
    List<Notificacion> findByIdVendedorAndIdAnuncioOrderByFechaCreacionDesc(String idVendedor, Long idAnuncio);
}

