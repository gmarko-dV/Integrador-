package com.integrador.repository;

import com.integrador.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    
    @Query("SELECT DISTINCT a FROM Anuncio a LEFT JOIN FETCH a.imagenes WHERE a.idUsuario = :idUsuario")
    List<Anuncio> findByIdUsuario(@Param("idUsuario") String idUsuario);
    
    List<Anuncio> findByActivoTrueOrderByFechaCreacionDesc();
    
    @Query("SELECT DISTINCT a FROM Anuncio a LEFT JOIN FETCH a.imagenes WHERE a.activo = true ORDER BY a.fechaCreacion DESC")
    List<Anuncio> findAllActivos();
    
    @Query("SELECT DISTINCT a FROM Anuncio a LEFT JOIN FETCH a.imagenes WHERE a.idAnuncio = :id")
    java.util.Optional<Anuncio> findByIdWithImagenes(@Param("id") Long id);
}

