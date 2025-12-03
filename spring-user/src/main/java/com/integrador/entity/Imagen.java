package com.integrador.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes")
public class Imagen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_anuncio", nullable = false)
    private Anuncio anuncio;
    
    @Column(name = "url_imagen", columnDefinition = "TEXT", nullable = false)
    private String urlImagen;
    
    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;
    
    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;
    
    @Column(name = "tamano_archivo")
    private Long tamanoArchivo;
    
    @Column(name = "fecha_subida")
    @CreationTimestamp
    private LocalDateTime fechaSubida;
    
    @Column(name = "orden")
    private Integer orden;
    
    // Constructores
    public Imagen() {}
    
    public Imagen(Anuncio anuncio, String urlImagen, String nombreArchivo, 
                  String tipoArchivo, Long tamanoArchivo, Integer orden) {
        this.anuncio = anuncio;
        this.urlImagen = urlImagen;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamanoArchivo = tamanoArchivo;
        this.orden = orden;
    }
    
    // Getters y Setters
    public Long getIdImagen() {
        return idImagen;
    }
    
    public void setIdImagen(Long idImagen) {
        this.idImagen = idImagen;
    }
    
    @JsonIgnore
    public Anuncio getAnuncio() {
        return anuncio;
    }
    
    public void setAnuncio(Anuncio anuncio) {
        this.anuncio = anuncio;
    }
    
    public String getUrlImagen() {
        return urlImagen;
    }
    
    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
    
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    
    public String getTipoArchivo() {
        return tipoArchivo;
    }
    
    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }
    
    public Long getTamanoArchivo() {
        return tamanoArchivo;
    }
    
    public void setTamanoArchivo(Long tamanoArchivo) {
        this.tamanoArchivo = tamanoArchivo;
    }
    
    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
    
    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}

