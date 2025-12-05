package com.integrador.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversaciones", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_anuncio", "id_vendedor", "id_comprador"}))
public class Conversacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long idConversacion;
    
    @Column(name = "id_anuncio", nullable = false)
    private Long idAnuncio;
    
    @Column(name = "id_vendedor", length = 255, nullable = false)
    private String idVendedor;
    
    @Column(name = "id_comprador", length = 255, nullable = false)
    private String idComprador;
    
    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_ultimo_mensaje")
    private LocalDateTime fechaUltimoMensaje;
    
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_anuncio", referencedColumnName = "id_anuncio", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Anuncio anuncio;
    
    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Mensaje> mensajes = new ArrayList<>();
    
    // Constructores
    public Conversacion() {}
    
    public Conversacion(Long idAnuncio, String idVendedor, String idComprador) {
        this.idAnuncio = idAnuncio;
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
    }
    
    // Getters y Setters
    public Long getIdConversacion() {
        return idConversacion;
    }
    
    public void setIdConversacion(Long idConversacion) {
        this.idConversacion = idConversacion;
    }
    
    public Long getIdAnuncio() {
        return idAnuncio;
    }
    
    public void setIdAnuncio(Long idAnuncio) {
        this.idAnuncio = idAnuncio;
    }
    
    public String getIdVendedor() {
        return idVendedor;
    }
    
    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }
    
    public String getIdComprador() {
        return idComprador;
    }
    
    public void setIdComprador(String idComprador) {
        this.idComprador = idComprador;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaUltimoMensaje() {
        return fechaUltimoMensaje;
    }
    
    public void setFechaUltimoMensaje(LocalDateTime fechaUltimoMensaje) {
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }
    
    public Boolean getActiva() {
        return activa;
    }
    
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
    
    public Anuncio getAnuncio() {
        return anuncio;
    }
    
    public void setAnuncio(Anuncio anuncio) {
        this.anuncio = anuncio;
    }
    
    public List<Mensaje> getMensajes() {
        return mensajes;
    }
    
    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }
    
    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        mensaje.setConversacion(this);
    }
    
    public void removeMensaje(Mensaje mensaje) {
        mensajes.remove(mensaje);
        mensaje.setConversacion(null);
    }
}

