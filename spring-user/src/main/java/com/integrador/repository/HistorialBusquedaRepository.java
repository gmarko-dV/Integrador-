package com.integrador.repository;

import com.integrador.entity.HistorialBusqueda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialBusquedaRepository extends JpaRepository<HistorialBusqueda, Long> {
    
    List<HistorialBusqueda> findByIdUsuarioOrderByFechaConsultaDesc(String idUsuario);
    
    @Query("SELECT h FROM HistorialBusqueda h WHERE h.idUsuario = :idUsuario ORDER BY h.fechaConsulta DESC")
    List<HistorialBusqueda> findTop5ByIdUsuarioOrderByFechaConsultaDesc(@Param("idUsuario") String idUsuario);
    
    @Query("SELECT h FROM HistorialBusqueda h WHERE h.placaConsultada = :placa ORDER BY h.fechaConsulta DESC")
    List<HistorialBusqueda> findByPlacaConsultadaOrderByFechaConsultaDesc(@Param("placa") String placa);
}
