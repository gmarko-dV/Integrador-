package com.tecsup.checkauto.model

data class Anuncio(
    val idAnuncio: Long? = null,
    val modelo: String,
    val anio: Int,
    val kilometraje: Int,
    val precio: Double,
    val descripcion: String,
    val emailContacto: String? = null,
    val telefonoContacto: String? = null,
    val fechaCreacion: String? = null,
    val idUsuario: String? = null,
    val imagenes: List<Imagen>? = null,
    val titulo: String? = null,
    val tipoVehiculo: String? = null,
    val idCategoria: Int? = null
)

data class Imagen(
    val idImagen: Long? = null,
    val urlImagen: String,
    val idAnuncio: Long? = null
)

