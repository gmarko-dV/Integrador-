package com.integrador.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;
    
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "id_vendedor", length = 255)
    private String idVendedor;
    
    @Column(name = "id_comprador", length = 255)
    private String idComprador;
    
    @Column(name = "nombre_comprador", length = 255)
    private String nombreComprador;
    
    @Column(name = "email_comprador", length = 255)
    private String emailComprador;
    
    @Column(name = "id_anuncio")
    private Long idAnuncio;
    
    @Column(name = "titulo", length = 200)
    private String titulo;
    
    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "leido")
    private Boolean leido = false;
    
    @Column(name = "leida")
    private Boolean leida = false;
    
    @Column(name = "fecha_creacion")
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode metadata;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_anuncio", referencedColumnName = "id_anuncio", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Anuncio anuncio;
    
    // Constructores
    public Notificacion() {}
    
    public Notificacion(String idVendedor, String idComprador, Long idAnuncio, String mensaje) {
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.idAnuncio = idAnuncio;
        this.mensaje = mensaje;
        this.titulo = "Interés en tu anuncio";
        this.leida = false;
        this.leido = false;
    }
    
    public Notificacion(String idVendedor, String idComprador, Long idAnuncio, String titulo, String mensaje) {
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.idAnuncio = idAnuncio;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.leida = false;
        this.leido = false;
    }
    
    // Getters y Setters
    public Long getIdNotificacion() {
        return idNotificacion;
    }
    
    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
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
    
    public String getNombreComprador() {
        return nombreComprador;
    }
    
    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }
    
    public String getEmailComprador() {
        return emailComprador;
    }
    
    public void setEmailComprador(String emailComprador) {
        this.emailComprador = emailComprador;
    }
    
    public Long getIdAnuncio() {
        return idAnuncio;
    }
    
    public void setIdAnuncio(Long idAnuncio) {
        this.idAnuncio = idAnuncio;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public Boolean getLeida() {
        return leida;
    }
    
    public void setLeida(Boolean leida) {
        this.leida = leida;
        // Sincronizar ambos campos
        if (leida != null) {
            this.leido = leida;
        }
    }
    
    public Boolean getLeido() {
        return leido;
    }
    
    public void setLeido(Boolean leido) {
        this.leido = leido;
        // Sincronizar ambos campos
        if (leido != null) {
            this.leida = leido;
        }
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public JsonNode getMetadata() {
        return metadata;
    }
    
    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }
    
    public Anuncio getAnuncio() {
        return anuncio;
    }
    
    public void setAnuncio(Anuncio anuncio) {
        this.anuncio = anuncio;
    }
}

