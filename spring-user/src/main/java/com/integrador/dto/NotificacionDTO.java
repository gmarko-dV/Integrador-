package com.integrador.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class NotificacionDTO {
    private Long idNotificacion;
    private String idVendedor;
    private String idComprador;
    private String nombreComprador;
    private String emailComprador;
    private Long idAnuncio;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private Boolean leido;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    // Constructores
    public NotificacionDTO() {}
    
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
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
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
    }
    
    public Boolean getLeido() {
        return leido;
    }
    
    public void setLeido(Boolean leido) {
        this.leido = leido;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    // Método helper para convertir de Notificacion a NotificacionDTO
    public static NotificacionDTO fromEntity(com.integrador.entity.Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setIdNotificacion(notificacion.getIdNotificacion());
        dto.setIdVendedor(notificacion.getIdVendedor());
        dto.setIdComprador(notificacion.getIdComprador());
        dto.setNombreComprador(notificacion.getNombreComprador());
        dto.setEmailComprador(notificacion.getEmailComprador());
        dto.setIdAnuncio(notificacion.getIdAnuncio());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setLeida(notificacion.getLeida());
        dto.setLeido(notificacion.getLeido());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        return dto;
    }
}

