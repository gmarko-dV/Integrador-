package com.integrador.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje;
    
    @Column(name = "id_conversacion", nullable = false)
    private Long idConversacion;
    
    @Column(name = "id_remitente", length = 255, nullable = false)
    private String idRemitente;
    
    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;
    
    @Column(name = "leido", nullable = false)
    private Boolean leido = false;
    
    @Column(name = "fecha_envio")
    @CreationTimestamp
    private LocalDateTime fechaEnvio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", referencedColumnName = "id_conversacion", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Conversacion conversacion;
    
    // Constructores
    public Mensaje() {}
    
    public Mensaje(Long idConversacion, String idRemitente, String mensaje) {
        this.idConversacion = idConversacion;
        this.idRemitente = idRemitente;
        this.mensaje = mensaje;
    }
    
    // Getters y Setters
    public Long getIdMensaje() {
        return idMensaje;
    }
    
    public void setIdMensaje(Long idMensaje) {
        this.idMensaje = idMensaje;
    }
    
    public Long getIdConversacion() {
        return idConversacion;
    }
    
    public void setIdConversacion(Long idConversacion) {
        this.idConversacion = idConversacion;
    }
    
    public String getIdRemitente() {
        return idRemitente;
    }
    
    public void setIdRemitente(String idRemitente) {
        this.idRemitente = idRemitente;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public Boolean getLeido() {
        return leido;
    }
    
    public void setLeido(Boolean leido) {
        this.leido = leido;
    }
    
    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
    
    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
    
    public Conversacion getConversacion() {
        return conversacion;
    }
    
    public void setConversacion(Conversacion conversacion) {
        this.conversacion = conversacion;
    }
}

