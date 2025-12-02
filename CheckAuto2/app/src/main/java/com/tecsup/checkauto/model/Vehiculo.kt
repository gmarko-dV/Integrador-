package com.tecsup.checkauto.model

data class Vehiculo(
    val placa: String,
    val marca: String? = null,
    val modelo: String? = null,
    val anio_registro_api: String? = null,
    val vin: String? = null,
    val uso: String? = null,
    val propietario: String? = null,
    val fecha_registro_api: String? = null,
    val delivery_point: String? = null,
    val descripcion_api: String? = null,
    val image_url_api: String? = null
)

