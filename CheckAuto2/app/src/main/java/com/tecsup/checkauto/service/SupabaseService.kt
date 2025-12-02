package com.tecsup.checkauto.service

import com.tecsup.checkauto.config.SupabaseConfig
import com.tecsup.checkauto.model.Anuncio
import com.tecsup.checkauto.model.Vehiculo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

// ========================================
// MODELOS DE DATOS PARA SUPABASE
// ========================================

@Serializable
data class AnuncioSupabase(
    val id_anuncio: Int? = null,
    val id_usuario: String,
    val titulo: String? = null,
    val modelo: String,
    val anio: Int,
    val kilometraje: Int,
    val precio: Double,
    val descripcion: String,
    val email_contacto: String? = null,
    val telefono_contacto: String? = null,
    val tipo_vehiculo: String? = null,
    val fecha_creacion: String? = null,
    val fecha_actualizacion: String? = null,
    val activo: Boolean = true
)

@Serializable
data class ImagenSupabase(
    val id_imagen: Int? = null,
    val id_anuncio: Int,
    val url_imagen: String,
    val nombre_archivo: String? = null,
    val tipo_archivo: String? = null,
    val tamano_archivo: Long? = null,
    val fecha_subida: String? = null,
    val orden: Int = 0
)

@Serializable
data class NotificacionSupabase(
    val id_notificacion: Int? = null,
    val id_usuario: Int? = null,
    val id_vendedor: String? = null,
    val id_comprador: String? = null,
    val nombre_comprador: String? = null,
    val email_comprador: String? = null,
    val id_anuncio: Int? = null,
    val titulo: String? = null,
    val mensaje: String? = null,
    val leido: Boolean = false,
    val leida: Boolean = false,
    val fecha_creacion: String? = null,
    val metadata: String? = null,
    val tipo: String = "interes"
)

@Serializable
data class VehiculoSupabase(
    val id_vehiculo: Int? = null,
    val placa: String? = null,
    val descripcion_api: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val anio_registro_api: String? = null,
    val vin: String? = null,
    val uso: String? = null,
    val propietario: String? = null,
    val delivery_point: String? = null,
    val fecha_registro_api: String? = null,
    val image_url_api: String? = null,
    val datos_api: String? = null,
    val fecha_actualizacion_api: String? = null,
    val tipo_vehiculo: String? = null
)

@Serializable
data class HistorialBusquedaSupabase(
    val id_historial: Int? = null,
    val id_usuario: String? = null,
    val placa_consultada: String? = null,
    val fecha_consulta: String? = null,
    val resultado_api: String? = null
)

// ========================================
// SERVICIO DE SUPABASE
// ========================================

object SupabaseService {
    private val client = SupabaseConfig.client
    
    // ========================================
    // ANUNCIOS
    // ========================================
    
    suspend fun getAnuncios(): List<AnuncioSupabase> {
        return client.from("anuncios")
            .select {
                filter {
                    eq("activo", true)
                }
                order("fecha_creacion", Order.DESCENDING)
            }
            .decodeList<AnuncioSupabase>()
    }
    
    suspend fun getAnuncioById(id: Int): AnuncioSupabase? {
        return client.from("anuncios")
            .select {
                filter {
                    eq("id_anuncio", id)
                }
            }
            .decodeSingleOrNull<AnuncioSupabase>()
    }
    
    suspend fun getAnunciosByUserId(userId: String): List<AnuncioSupabase> {
        return client.from("anuncios")
            .select {
                filter {
                    eq("id_usuario", userId)
                }
                order("fecha_creacion", Order.DESCENDING)
            }
            .decodeList<AnuncioSupabase>()
    }
    
    suspend fun createAnuncio(anuncio: AnuncioSupabase): AnuncioSupabase {
        return client.from("anuncios")
            .insert(anuncio) {
                select()
            }
            .decodeSingle<AnuncioSupabase>()
    }
    
    suspend fun updateAnuncio(id: Int, anuncio: AnuncioSupabase): AnuncioSupabase {
        return client.from("anuncios")
            .update(anuncio) {
                filter {
                    eq("id_anuncio", id)
                }
                select()
            }
            .decodeSingle<AnuncioSupabase>()
    }
    
    suspend fun deleteAnuncio(id: Int) {
        client.from("anuncios")
            .update(mapOf("activo" to false)) {
                filter {
                    eq("id_anuncio", id)
                }
            }
    }
    
    // ========================================
    // IMÁGENES
    // ========================================
    
    suspend fun getImagenesByAnuncioId(anuncioId: Int): List<ImagenSupabase> {
        return client.from("imagenes")
            .select {
                filter {
                    eq("id_anuncio", anuncioId)
                }
                order("orden", Order.ASCENDING)
            }
            .decodeList<ImagenSupabase>()
    }
    
    suspend fun addImagen(imagen: ImagenSupabase): ImagenSupabase {
        return client.from("imagenes")
            .insert(imagen) {
                select()
            }
            .decodeSingle<ImagenSupabase>()
    }
    
    suspend fun deleteImagen(id: Int) {
        client.from("imagenes")
            .delete {
                filter {
                    eq("id_imagen", id)
                }
            }
    }
    
    // ========================================
    // NOTIFICACIONES
    // ========================================
    
    suspend fun getNotificacionesByVendedor(vendedorId: String): List<NotificacionSupabase> {
        return client.from("notificaciones")
            .select {
                filter {
                    eq("id_vendedor", vendedorId)
                }
                order("fecha_creacion", Order.DESCENDING)
            }
            .decodeList<NotificacionSupabase>()
    }
    
    suspend fun getUnreadNotificaciones(vendedorId: String): List<NotificacionSupabase> {
        return client.from("notificaciones")
            .select {
                filter {
                    eq("id_vendedor", vendedorId)
                    eq("leido", false)
                }
                order("fecha_creacion", Order.DESCENDING)
            }
            .decodeList<NotificacionSupabase>()
    }
    
    suspend fun createNotificacion(notificacion: NotificacionSupabase): NotificacionSupabase {
        return client.from("notificaciones")
            .insert(notificacion) {
                select()
            }
            .decodeSingle<NotificacionSupabase>()
    }
    
    suspend fun markNotificacionAsRead(id: Int) {
        client.from("notificaciones")
            .update(mapOf("leido" to true, "leida" to true)) {
                filter {
                    eq("id_notificacion", id)
                }
            }
    }
    
    suspend fun markAllNotificacionesAsRead(vendedorId: String) {
        client.from("notificaciones")
            .update(mapOf("leido" to true, "leida" to true)) {
                filter {
                    eq("id_vendedor", vendedorId)
                    eq("leido", false)
                }
            }
    }
    
    // ========================================
    // VEHÍCULOS
    // ========================================
    
    suspend fun getVehiculoByPlaca(placa: String): VehiculoSupabase? {
        return client.from("vehiculos")
            .select {
                filter {
                    eq("placa", placa)
                }
            }
            .decodeSingleOrNull<VehiculoSupabase>()
    }
    
    suspend fun upsertVehiculo(vehiculo: VehiculoSupabase): VehiculoSupabase {
        return client.from("vehiculos")
            .upsert(vehiculo) {
                select()
            }
            .decodeSingle<VehiculoSupabase>()
    }
    
    // ========================================
    // HISTORIAL DE BÚSQUEDA
    // ========================================
    
    suspend fun getHistorialByUserId(userId: String, limit: Int = 10): List<HistorialBusquedaSupabase> {
        return client.from("historial_busqueda")
            .select {
                filter {
                    eq("id_usuario", userId)
                }
                order("fecha_consulta", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<HistorialBusquedaSupabase>()
    }
    
    suspend fun addHistorial(historial: HistorialBusquedaSupabase): HistorialBusquedaSupabase {
        return client.from("historial_busqueda")
            .insert(historial) {
                select()
            }
            .decodeSingle<HistorialBusquedaSupabase>()
    }
}

