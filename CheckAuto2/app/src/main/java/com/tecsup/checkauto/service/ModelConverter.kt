package com.tecsup.checkauto.service

import com.tecsup.checkauto.model.Anuncio
import com.tecsup.checkauto.model.Imagen

/**
 * Funciones de conversión entre modelos de la app y modelos de Supabase
 */
object ModelConverter {
    
    /**
     * Convierte AnuncioSupabase a Anuncio (modelo de la app)
     */
    fun anuncioSupabaseToAnuncio(
        anuncioSupabase: AnuncioSupabase,
        imagenes: List<Imagen> = emptyList()
    ): Anuncio {
        return Anuncio(
            idAnuncio = anuncioSupabase.id_anuncio?.toLong(),
            modelo = anuncioSupabase.modelo,
            anio = anuncioSupabase.anio,
            kilometraje = anuncioSupabase.kilometraje,
            precio = anuncioSupabase.precio,
            descripcion = anuncioSupabase.descripcion,
            emailContacto = anuncioSupabase.email_contacto,
            telefonoContacto = anuncioSupabase.telefono_contacto,
            fechaCreacion = anuncioSupabase.fecha_creacion,
            idUsuario = anuncioSupabase.id_usuario,
            imagenes = imagenes,
            titulo = anuncioSupabase.titulo,
            tipoVehiculo = anuncioSupabase.tipo_vehiculo
        )
    }
    
    /**
     * Convierte Anuncio (modelo de la app) a AnuncioSupabase
     */
    fun anuncioToAnuncioSupabase(anuncio: Anuncio): AnuncioSupabase {
        return AnuncioSupabase(
            id_anuncio = anuncio.idAnuncio?.toInt(),
            id_usuario = anuncio.idUsuario ?: "",
            titulo = anuncio.titulo,
            modelo = anuncio.modelo,
            anio = anuncio.anio,
            kilometraje = anuncio.kilometraje,
            precio = anuncio.precio,
            descripcion = anuncio.descripcion,
            email_contacto = anuncio.emailContacto,
            telefono_contacto = anuncio.telefonoContacto,
            tipo_vehiculo = anuncio.tipoVehiculo,
            fecha_creacion = anuncio.fechaCreacion,
            activo = true
        )
    }
    
    /**
     * Convierte ImagenSupabase a Imagen (modelo de la app)
     */
    fun imagenSupabaseToImagen(imagenSupabase: ImagenSupabase): Imagen {
        return Imagen(
            idImagen = imagenSupabase.id_imagen?.toLong(),
            urlImagen = imagenSupabase.url_imagen,
            idAnuncio = imagenSupabase.id_anuncio.toLong()
        )
    }
    
    /**
     * Convierte Imagen (modelo de la app) a ImagenSupabase
     */
    fun imagenToImagenSupabase(imagen: Imagen, orden: Int = 0): ImagenSupabase {
        return ImagenSupabase(
            id_imagen = imagen.idImagen?.toInt(),
            id_anuncio = imagen.idAnuncio?.toInt() ?: 0,
            url_imagen = imagen.urlImagen,
            orden = orden
        )
    }
}

