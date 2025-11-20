package com.integrador.repository;

import com.integrador.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    
    List<Anuncio> findByIdUsuario(String idUsuario);
    
    List<Anuncio> findByActivoTrueOrderByFechaCreacionDesc();
    
    @Query("SELECT a FROM Anuncio a WHERE a.activo = true ORDER BY a.fechaCreacion DESC")
    List<Anuncio> findAllActivos();
}

