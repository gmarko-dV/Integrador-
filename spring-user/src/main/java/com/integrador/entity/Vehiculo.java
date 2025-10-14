package com.integrador.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long idVehiculo;
    
    @Column(name = "placa", unique = true, length = 20)
    private String placa;
    
    @Column(name = "descripcion_api", columnDefinition = "TEXT")
    private String descripcionApi;
    
    @Column(name = "marca", length = 100)
    private String marca;
    
    @Column(name = "modelo", length = 100)
    private String modelo;
    
    @Column(name = "anio_registro_api", length = 10)
    private String anioRegistroApi;
    
    @Column(name = "vin", length = 100)
    private String vin;
    
    @Column(name = "uso", length = 200)
    private String uso;
    
    @Column(name = "propietario", columnDefinition = "TEXT")
    private String propietario;
    
    @Column(name = "delivery_point", columnDefinition = "TEXT")
    private String deliveryPoint;
    
    @Column(name = "fecha_registro_api")
    private LocalDateTime fechaRegistroApi;
    
    @Column(name = "image_url_api", columnDefinition = "TEXT")
    private String imageUrlApi;
    
    @Column(name = "datos_api", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode datosApi;
    
    @Column(name = "fecha_actualizacion_api")
    @UpdateTimestamp
    private LocalDateTime fechaActualizacionApi;
    
    // Constructores
    public Vehiculo() {}
    
    public Vehiculo(String placa, String descripcionApi, String marca, String modelo, 
                   String anioRegistroApi, String vin, String uso, String propietario, 
                   String deliveryPoint, LocalDateTime fechaRegistroApi, String imageUrlApi, 
                   JsonNode datosApi) {
        this.placa = placa;
        this.descripcionApi = descripcionApi;
        this.marca = marca;
        this.modelo = modelo;
        this.anioRegistroApi = anioRegistroApi;
        this.vin = vin;
        this.uso = uso;
        this.propietario = propietario;
        this.deliveryPoint = deliveryPoint;
        this.fechaRegistroApi = fechaRegistroApi;
        this.imageUrlApi = imageUrlApi;
        this.datosApi = datosApi;
    }
    
    // Getters y Setters
    public Long getIdVehiculo() {
        return idVehiculo;
    }
    
    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }
    
    public String getPlaca() {
        return placa;
    }
    
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    
    public String getDescripcionApi() {
        return descripcionApi;
    }
    
    public void setDescripcionApi(String descripcionApi) {
        this.descripcionApi = descripcionApi;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public String getAnioRegistroApi() {
        return anioRegistroApi;
    }
    
    public void setAnioRegistroApi(String anioRegistroApi) {
        this.anioRegistroApi = anioRegistroApi;
    }
    
    public String getVin() {
        return vin;
    }
    
    public void setVin(String vin) {
        this.vin = vin;
    }
    
    public String getUso() {
        return uso;
    }
    
    public void setUso(String uso) {
        this.uso = uso;
    }
    
    public String getPropietario() {
        return propietario;
    }
    
    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }
    
    public String getDeliveryPoint() {
        return deliveryPoint;
    }
    
    public void setDeliveryPoint(String deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }
    
    public LocalDateTime getFechaRegistroApi() {
        return fechaRegistroApi;
    }
    
    public void setFechaRegistroApi(LocalDateTime fechaRegistroApi) {
        this.fechaRegistroApi = fechaRegistroApi;
    }
    
    public String getImageUrlApi() {
        return imageUrlApi;
    }
    
    public void setImageUrlApi(String imageUrlApi) {
        this.imageUrlApi = imageUrlApi;
    }
    
    public JsonNode getDatosApi() {
        return datosApi;
    }
    
    public void setDatosApi(JsonNode datosApi) {
        this.datosApi = datosApi;
    }
    
    // Método helper para convertir String a JsonNode
    public void setDatosApiFromString(String datosApiJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.datosApi = mapper.readTree(datosApiJson);
        } catch (Exception e) {
            this.datosApi = null;
        }
    }
    
    public LocalDateTime getFechaActualizacionApi() {
        return fechaActualizacionApi;
    }
    
    public void setFechaActualizacionApi(LocalDateTime fechaActualizacionApi) {
        this.fechaActualizacionApi = fechaActualizacionApi;
    }
}
