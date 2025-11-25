package com.integrador.dto;

import java.math.BigDecimal;

public class AnuncioRequest {
    private String modelo;
    private Integer anio;
    private Integer kilometraje;
    private BigDecimal precio;
    private String descripcion;
    private String emailContacto;
    private String telefonoContacto;
    private String tipoVehiculo;
    
    // Constructores
    public AnuncioRequest() {}
    
    public AnuncioRequest(String modelo, Integer anio, Integer kilometraje, 
                         BigDecimal precio, String descripcion) {
        this.modelo = modelo;
        this.anio = anio;
        this.kilometraje = kilometraje;
        this.precio = precio;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
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
    
    public String getEmailContacto() {
        return emailContacto;
    }
    
    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }
    
    public String getTelefonoContacto() {
        return telefonoContacto;
    }
    
    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }
    
    public String getTipoVehiculo() {
        return tipoVehiculo;
    }
    
    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }
}

