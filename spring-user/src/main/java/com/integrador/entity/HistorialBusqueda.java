package com.integrador.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_busqueda")
public class HistorialBusqueda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;
    
    @Column(name = "id_usuario", length = 255)
    private String idUsuario;
    
    @Column(name = "placa_consultada", length = 20)
    private String placaConsultada;
    
    @Column(name = "fecha_consulta")
    @CreationTimestamp
    private LocalDateTime fechaConsulta;
    
    @Column(name = "resultado_api", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode resultadoApi;
    
    // Constructores
    public HistorialBusqueda() {}
    
    public HistorialBusqueda(String idUsuario, String placaConsultada, JsonNode resultadoApi) {
        this.idUsuario = idUsuario;
        this.placaConsultada = placaConsultada;
        this.resultadoApi = resultadoApi;
    }
    
    // Constructor helper para String
    public HistorialBusqueda(String idUsuario, String placaConsultada, String resultadoApiJson) {
        this.idUsuario = idUsuario;
        this.placaConsultada = placaConsultada;
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            this.resultadoApi = mapper.readTree(resultadoApiJson);
        } catch (Exception e) {
            this.resultadoApi = mapper.createObjectNode();
        }
    }
    
    // Getters y Setters
    public Long getIdHistorial() {
        return idHistorial;
    }
    
    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }
    
    public String getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getPlacaConsultada() {
        return placaConsultada;
    }
    
    public void setPlacaConsultada(String placaConsultada) {
        this.placaConsultada = placaConsultada;
    }
    
    public LocalDateTime getFechaConsulta() {
        return fechaConsulta;
    }
    
    public void setFechaConsulta(LocalDateTime fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }
    
    public JsonNode getResultadoApi() {
        return resultadoApi;
    }
    
    public void setResultadoApi(JsonNode resultadoApi) {
        this.resultadoApi = resultadoApi;
    }
    
    // Método helper para convertir String a JsonNode
    public void setResultadoApiFromString(String resultadoApiJson) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            this.resultadoApi = mapper.readTree(resultadoApiJson);
        } catch (Exception e) {
            this.resultadoApi = mapper.createObjectNode();
        }
    }
}
