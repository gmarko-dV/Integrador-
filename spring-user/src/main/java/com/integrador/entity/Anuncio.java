package com.integrador.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anuncios")
public class Anuncio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anuncio")
    private Long idAnuncio;
    
    @Column(name = "id_usuario", length = 255, nullable = false)
    private String idUsuario;
    
    @Column(name = "titulo", length = 200)
    private String titulo;
    
    @Column(name = "modelo", length = 100, nullable = false)
    private String modelo;
    
    @Column(name = "anio", nullable = false)
    private Integer anio;
    
    @Column(name = "kilometraje", nullable = false)
    private Integer kilometraje;
    
    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal precio;
    
    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;
    
    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>();
    
    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    // Constructores
    public Anuncio() {}
    
    public Anuncio(String idUsuario, String modelo, Integer anio, Integer kilometraje, 
                   BigDecimal precio, String descripcion) {
        this.idUsuario = idUsuario;
        this.modelo = modelo;
        this.anio = anio;
        this.kilometraje = kilometraje;
        this.precio = precio;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Long getIdAnuncio() {
        return idAnuncio;
    }
    
    public void setIdAnuncio(Long idAnuncio) {
        this.idAnuncio = idAnuncio;
    }
    
    public String getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public Integer getAnio() {
        return anio;
    }
    
    public void setAnio(Integer anio) {
        this.anio = anio;
    }
    
    public Integer getKilometraje() {
        return kilometraje;
    }
    
    public void setKilometraje(Integer kilometraje) {
        this.kilometraje = kilometraje;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<Imagen> getImagenes() {
        return imagenes;
    }
    
    public void setImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
    }
    
    public void addImagen(Imagen imagen) {
        imagenes.add(imagen);
        imagen.setAnuncio(this);
    }
    
    public void removeImagen(Imagen imagen) {
        imagenes.remove(imagen);
        imagen.setAnuncio(null);
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

