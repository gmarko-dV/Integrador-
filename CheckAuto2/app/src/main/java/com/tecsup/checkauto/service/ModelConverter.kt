package com.tecsup.checkauto.service

import com.tecsup.checkauto.config.ApiConfig
import com.tecsup.checkauto.config.SupabaseConfig
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
     * Convierte URLs relativas a URLs completas de Supabase Storage si es necesario
     */
    fun imagenSupabaseToImagen(imagenSupabase: ImagenSupabase): Imagen {
        val urlImagen = normalizarUrlImagen(imagenSupabase.url_imagen)
        return Imagen(
            idImagen = imagenSupabase.id_imagen?.toLong(),
            urlImagen = urlImagen,
            idAnuncio = imagenSupabase.id_anuncio.toLong()
        )
    }
    
    /**
     * Normaliza la URL de la imagen para usar Supabase Storage:
     * - Si es una URL completa (http/https), la devuelve tal cual
     * - Si es una ruta relativa (empieza con /uploads/), la convierte a Supabase Storage
     * 
     * La app móvil es independiente y usa Supabase Storage para todas las imágenes.
     * Las URLs relativas /uploads/... se convierten a Supabase Storage.
     */
    private fun normalizarUrlImagen(url: String): String {
        android.util.Log.d("ModelConverter", "Normalizando URL de imagen original: $url")
        
        val urlNormalizada = when {
            // Si ya es una URL completa (http/https), verificar si es de Supabase Storage
            url.startsWith("http://") || url.startsWith("https://") -> {
                // Si ya es una URL de Supabase Storage, usarla tal cual
                if (url.contains("supabase.co/storage")) {
                    android.util.Log.d("ModelConverter", "URL ya es de Supabase Storage, usando tal cual")
                    url
                } else {
                    // Si es una URL de otro servidor (Spring Boot, Django), intentar convertirla
                    // Extraer el nombre del archivo de la URL
                    val fileName = url.substringAfterLast("/")
                    val supabaseUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET_ANUNCIOS}/$fileName"
                    android.util.Log.d("ModelConverter", "URL externa convertida a Supabase Storage: $supabaseUrl")
                    supabaseUrl
                }
            }
            // Si es una ruta relativa que empieza con /uploads/, convertirla a Supabase Storage
            url.startsWith("/uploads/") -> {
                // Extraer el path después de /uploads/
                val path = url.removePrefix("/uploads/")
                // Construir URL completa de Supabase Storage
                val supabaseUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET_ANUNCIOS}/$path"
                android.util.Log.d("ModelConverter", "URL relativa /uploads/ convertida a Supabase Storage: $supabaseUrl")
                supabaseUrl
            }
            // Si es una ruta relativa sin /uploads/, puede ser un path directo del bucket
            url.startsWith("/") -> {
                val path = url.removePrefix("/")
                val supabaseUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET_ANUNCIOS}/$path"
                android.util.Log.d("ModelConverter", "URL relativa / convertida a Supabase Storage: $supabaseUrl")
                supabaseUrl
            }
            // Si no empieza con /, puede ser un path relativo del bucket (sin / inicial)
            else -> {
                val supabaseUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET_ANUNCIOS}/$url"
                android.util.Log.d("ModelConverter", "Path relativo convertido a Supabase Storage: $supabaseUrl")
                supabaseUrl
            }
        }
        
        android.util.Log.d("ModelConverter", "URL final normalizada: $urlNormalizada")
        return urlNormalizada
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

