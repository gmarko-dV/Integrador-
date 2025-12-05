package com.tecsup.checkauto.model

data class Conversacion(
    val idConversacion: Long? = null,
    val idAnuncio: Long,
    val idVendedor: String,
    val idComprador: String,
    val fechaCreacion: String? = null,
    val fechaUltimoMensaje: String? = null,
    val activa: Boolean = true,
    val mensajes: List<Mensaje>? = null
)

data class Mensaje(
    val idMensaje: Long? = null,
    val idConversacion: Long,
    val idRemitente: String,
    val mensaje: String,
    val leido: Boolean = false,
    val fechaEnvio: String? = null
)

