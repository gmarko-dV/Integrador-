package com.tecsup.checkauto.api

import com.tecsup.checkauto.model.Vehiculo

data class PlateSearchResponse(
    val success: Boolean,
    val message: String,
    val vehicle: VehicleInfo?,
    val error: String? = null
)

data class VehicleInfo(
    val placa: String,
    val marca: String?,
    val modelo: String?,
    val anio_registro_api: String?,
    val descripcion_api: String?,
    val propietario: String?,
    val vin: String?,
    val image_url_api: String?,
    val uso: String?,
    val delivery_point: String?,
    val fecha_registro_api: String?,
    val tamano_motor: String? = null,
    val tipo_combustible: String? = null,
    val numero_asientos: String? = null
) {
    fun toVehiculo(): Vehiculo {
        return Vehiculo(
            placa = placa,
            marca = marca,
            modelo = modelo,
            anio_registro_api = anio_registro_api,
            vin = vin,
            uso = uso,
            propietario = propietario,
            fecha_registro_api = fecha_registro_api,
            delivery_point = delivery_point,
            descripcion_api = descripcion_api,
            image_url_api = image_url_api
        )
    }
}

