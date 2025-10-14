package com.integrador.repository;

import com.integrador.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    
    Optional<Vehiculo> findByPlaca(String placa);
    
    boolean existsByPlaca(String placa);
    
    @Query("SELECT v FROM Vehiculo v WHERE v.marca ILIKE %:marca% AND v.modelo ILIKE %:modelo%")
    List<Vehiculo> findByMarcaAndModelo(@Param("marca") String marca, @Param("modelo") String modelo);
    
    @Query("SELECT v FROM Vehiculo v WHERE v.anioRegistroApi = :anio")
    List<Vehiculo> findByAnioRegistroApi(@Param("anio") String anio);
    
    @Query("SELECT v FROM Vehiculo v ORDER BY v.fechaActualizacionApi DESC")
    List<Vehiculo> findTop10ByOrderByFechaActualizacionApiDesc();
}
