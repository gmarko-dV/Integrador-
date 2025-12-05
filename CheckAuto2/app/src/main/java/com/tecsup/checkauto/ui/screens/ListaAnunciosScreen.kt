package com.tecsup.checkauto.ui.screens

import android.util.Log
import com.tecsup.checkauto.service.AnuncioService
import com.tecsup.checkauto.service.SupabaseAuthService
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tecsup.checkauto.model.Anuncio
import com.tecsup.checkauto.service.SupabaseService
import com.tecsup.checkauto.service.ModelConverter
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun ListaAnunciosScreen(
    tipoVehiculo: String? = null,
    esMisAnuncios: Boolean = false,
    onAnuncioClick: (Long) -> Unit = {},
    onContactar: (Anuncio) -> Unit = {},
    onEliminar: (Long) -> Unit = {},
    isAuthenticated: Boolean = false,
    userId: String? = null
) {
    var anuncios by remember { mutableStateOf<List<Anuncio>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar anuncios desde Supabase
    LaunchedEffect(esMisAnuncios, userId, tipoVehiculo) {
        isLoading = true
        errorMessage = null
        try {
            // Si es "Mis Anuncios", el userId debe estar presente
            if (esMisAnuncios && userId == null) {
                errorMessage = "Debes estar autenticado para ver tus anuncios"
                anuncios = emptyList()
                isLoading = false
                return@LaunchedEffect
            }
            
            val anunciosSupabase = if (esMisAnuncios && userId != null) {
                android.util.Log.d("ListaAnunciosScreen", "Cargando anuncios del usuario: $userId")
                val anunciosUsuario = SupabaseService.getAnunciosByUserId(userId)
                android.util.Log.d("ListaAnunciosScreen", "Anuncios encontrados: ${anunciosUsuario.size}")
                anunciosUsuario.forEach { anuncio ->
                    android.util.Log.d("ListaAnunciosScreen", "Anuncio ID: ${anuncio.id_anuncio}, Usuario: ${anuncio.id_usuario}")
                }
                anunciosUsuario
            } else {
                SupabaseService.getAnuncios()
            }
            
            // Convertir y cargar imágenes para cada anuncio
            val anunciosConImagenes = anunciosSupabase.map { anuncioSupabase ->
                val imagenesSupabase = try {
                    val imagenes = SupabaseService.getImagenesByAnuncioId(anuncioSupabase.id_anuncio ?: 0)
                    android.util.Log.d("ListaAnunciosScreen", "📸 Anuncio ${anuncioSupabase.id_anuncio}: ${imagenes.size} imágenes encontradas en BD")
                    imagenes.forEachIndexed { index, img ->
                        android.util.Log.d("ListaAnunciosScreen", "  📷 Imagen $index: id=${img.id_imagen}, url_original=${img.url_imagen}, orden=${img.orden}")
                    }
                    imagenes
                } catch (e: Exception) {
                    android.util.Log.e("ListaAnunciosScreen", "❌ Error al obtener imágenes para anuncio ${anuncioSupabase.id_anuncio}: ${e.message}", e)
                    emptyList()
                }
                val imagenes = imagenesSupabase.map { 
                    val imagenConvertida = ModelConverter.imagenSupabaseToImagen(it)
                    android.util.Log.d("ListaAnunciosScreen", "✅ Imagen convertida para anuncio ${anuncioSupabase.id_anuncio}: url_normalizada=${imagenConvertida.urlImagen}")
                    imagenConvertida
                }
                val anuncioFinal = ModelConverter.anuncioSupabaseToAnuncio(anuncioSupabase, imagenes)
                android.util.Log.d("ListaAnunciosScreen", "📋 Anuncio ${anuncioSupabase.id_anuncio} finalizado: ${anuncioFinal.imagenes?.size ?: 0} imágenes asignadas")
                anuncioFinal
            }
            
            anuncios = anunciosConImagenes
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error al cargar anuncios: ${e.message}"
            android.util.Log.e("ListaAnunciosScreen", "Error al cargar anuncios", e)
            isLoading = false
        }
    }
    var imagenActual by remember { mutableStateOf(mapOf<Long, Int>()) }
    var tipoFiltro by remember { mutableStateOf(tipoVehiculo) }

    // Filtrar anuncios por tipo y por usuario si es "Mis Anuncios"
    val anunciosFiltrados = remember(anuncios, tipoFiltro, esMisAnuncios, userId) {
        anuncios.filter { anuncio ->
            // Filtro por tipo de vehículo
            val cumpleTipo = tipoFiltro == null || anuncio.tipoVehiculo?.equals(tipoFiltro, ignoreCase = true) == true
            
            // Filtro por usuario (solo si es "Mis Anuncios")
            val esMio = if (esMisAnuncios) {
                if (userId == null) {
                    false // Si no hay userId, no mostrar nada
                } else {
                    // Comparación estricta del ID de usuario
                    anuncio.idUsuario?.equals(userId, ignoreCase = false) == true
                }
            } else {
                true // Si no es "Mis Anuncios", mostrar todos
            }
            
            cumpleTipo && esMio
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1F2E), // Azul muy oscuro que complementa el header
                        Color(0xFF0F1419)  // Negro azulado más suave
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header azul claro como la web
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0066CC),
                                Color(0xFF0052A3)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = if (tipoFiltro != null) "Vehículos $tipoFiltro" else if (esMisAnuncios) "Tus Anuncios" else "Vehículos en Venta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${anunciosFiltrados.size} ${if (anunciosFiltrados.size == 1) "anuncio" else "anuncios"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Text(
                            text = "disponibles",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFFF6B6B).copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF0066CC)
                    )
                }
            } else if (anunciosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            color = Color(0xFF0066CC).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(120.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFF0066CC).copy(alpha = 0.3f)
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "🚗",
                                    fontSize = 64.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (esMisAnuncios) 
                                "Aún no tienes anuncios publicados"
                            else if (tipoFiltro != null) 
                                "No hay anuncios disponibles para $tipoFiltro"
                            else 
                                "No hay anuncios disponibles",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(anunciosFiltrados) { anuncio ->
                        AnuncioCard(
                            anuncio = anuncio,
                            imagenActual = imagenActual[anuncio.idAnuncio] ?: 0,
                            onImagenChange = { idAnuncio: Long, index: Int ->
                                imagenActual = imagenActual + (idAnuncio to index)
                            },
                            onAnuncioClick = { onAnuncioClick(anuncio.idAnuncio ?: 0) },
                            onContactar = { onContactar(anuncio) },
                            onEliminar = { 
                                scope.launch {
                                    try {
                                        anuncio.idAnuncio?.let { id ->
                                            // Obtener token de autenticación
                                            val accessToken = SupabaseAuthService.getAccessToken()
                                            if (accessToken == null) {
                                                errorMessage = "Debes estar autenticado para eliminar un anuncio"
                                                return@launch
                                            }
                                            
                                            // Usar el servicio del backend de Spring Boot
                                            val anuncioService = AnuncioService()
                                            val result = anuncioService.deleteAnuncio(id, accessToken)
                                            
                                            if (result.isSuccess) {
                                                // Recargar anuncios después de eliminar
                                                val anunciosSupabase = if (esMisAnuncios && userId != null) {
                                                    SupabaseService.getAnunciosByUserId(userId)
                                                } else {
                                                    SupabaseService.getAnuncios()
                                                }
                                                val anunciosConImagenes = anunciosSupabase.map { anuncioSupabase ->
                                                    val imagenesSupabase = try {
                                                        SupabaseService.getImagenesByAnuncioId(anuncioSupabase.id_anuncio ?: 0)
                                                    } catch (e: Exception) {
                                                        emptyList()
                                                    }
                                                    val imagenes = imagenesSupabase.map { ModelConverter.imagenSupabaseToImagen(it) }
                                                    ModelConverter.anuncioSupabaseToAnuncio(anuncioSupabase, imagenes)
                                                }
                                                anuncios = anunciosConImagenes
                                                errorMessage = null // Limpiar error si se eliminó exitosamente
                                            } else {
                                                val exception = result.exceptionOrNull()
                                                val mensaje = when {
                                                    exception?.message?.contains("permiso", ignoreCase = true) == true ||
                                                    exception?.message?.contains("permission", ignoreCase = true) == true ||
                                                    exception?.message?.contains("dueño", ignoreCase = true) == true ||
                                                    exception?.message?.contains("owner", ignoreCase = true) == true ->
                                                        "Error de permisos. Verifica que seas el dueño del anuncio."
                                                    exception?.message?.contains("autenticado", ignoreCase = true) == true ||
                                                    exception?.message?.contains("authenticated", ignoreCase = true) == true ||
                                                    exception?.message?.contains("401") == true ->
                                                        "Debes estar autenticado para eliminar un anuncio"
                                                    else -> exception?.message ?: "Error al eliminar el anuncio"
                                                }
                                                errorMessage = mensaje
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ListaAnunciosScreen", "Error al eliminar anuncio: ${e.message}", e)
                                        val mensaje = when {
                                            e.message?.contains("row-level security", ignoreCase = true) == true -> 
                                                "Error de permisos. Verifica que seas el dueño del anuncio."
                                            e.message?.contains("violates", ignoreCase = true) == true -> 
                                                "Error de permisos. No puedes eliminar este anuncio."
                                            else -> "Error al eliminar: ${e.message ?: "Error desconocido"}"
                                        }
                                        errorMessage = mensaje
                                    }
                                }
                                onEliminar(anuncio.idAnuncio ?: 0) 
                            },
                            esMiAnuncio = esMisAnuncios && anuncio.idUsuario == userId,
                            isAuthenticated = isAuthenticated
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnuncioCard(
    anuncio: Anuncio,
    imagenActual: Int,
    onImagenChange: (Long, Int) -> Unit,
    onAnuncioClick: () -> Unit,
    onContactar: () -> Unit,
    onEliminar: () -> Unit,
    esMiAnuncio: Boolean,
    isAuthenticated: Boolean
) {
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val numberFormatter = NumberFormat.getNumberInstance(Locale("es", "PE"))
    
    val imagenes = anuncio.imagenes ?: emptyList()
    val tieneMultiplesImagenes = imagenes.size > 1
    val imagenMostrada = if (imagenes.isNotEmpty()) {
        imagenes.getOrNull(imagenActual)?.urlImagen
    } else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAnuncioClick),
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(0.dp)
        ) {
            // Imagen con controles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (imagenMostrada != null && imagenMostrada.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(imagenMostrada)
                                .crossfade(true)
                                .allowHardware(false) // Permitir todos los formatos de imagen
                                .error(android.R.drawable.ic_menu_report_image)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .listener(
                                    onStart = { 
                                        Log.d("ListaAnunciosScreen", "Cargando imagen para anuncio ${anuncio.idAnuncio}: $imagenMostrada") 
                                    },
                                    onSuccess = { _, _ -> 
                                        Log.d("ListaAnunciosScreen", "✅ Imagen cargada exitosamente para anuncio ${anuncio.idAnuncio}: $imagenMostrada") 
                                    },
                                    onError = { _, result -> 
                                        Log.e("ListaAnunciosScreen", "❌ Error al cargar imagen para anuncio ${anuncio.idAnuncio}: $imagenMostrada - ${result.throwable.message}", result.throwable)
                                    }
                                )
                                .build()
                        ),
                        contentDescription = anuncio.modelo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Log.d("ListaAnunciosScreen", "⚠️ No hay imagen para mostrar para anuncio ${anuncio.idAnuncio} (tiene ${imagenes.size} imágenes en total)")
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🚗",
                                    fontSize = 64.sp
                                )
                                if (imagenes.isEmpty()) {
                                    Text(
                                        text = "Sin imagen",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Badge de tipo de vehículo
                if (anuncio.tipoVehiculo != null) {
                    Surface(
                        color = Color(0xFF0066CC).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = anuncio.tipoVehiculo.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Controles de imagen si hay múltiples
                if (tieneMultiplesImagenes) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val nuevoIndice = (imagenActual - 1 + imagenes.size) % imagenes.size
                                onImagenChange(anuncio.idAnuncio ?: 0, nuevoIndice)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Anterior",
                                tint = Color.White
                            )
                        }
                        
                        // Indicadores
                        imagenes.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == imagenActual) Color.White else Color.White.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        onImagenChange(anuncio.idAnuncio ?: 0, index)
                                    }
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                val nuevoIndice = (imagenActual + 1) % imagenes.size
                                onImagenChange(anuncio.idAnuncio ?: 0, nuevoIndice)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Siguiente",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Información del anuncio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Título
                Text(
                    text = anuncio.titulo ?: "${anuncio.modelo} ${anuncio.anio}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Precio destacado
                Text(
                    text = formatter.format(anuncio.precio),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066CC),
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Detalles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (anuncio.tipoVehiculo != null) {
                        Surface(
                            color = Color(0xFF0066CC).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                Color(0xFF0066CC).copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("🚗", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = anuncio.tipoVehiculo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Surface(
                        color = Color(0xFF0066CC).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            Color(0xFF0066CC).copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("📊", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${numberFormatter.format(anuncio.kilometraje)} km",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                // Acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAnuncioClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0066CC)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            "Ver Detalles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                    }
                    
                    if (esMiAnuncio) {
                        Surface(
                            onClick = onEliminar,
                            modifier = Modifier.size(52.dp),
                            color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFFF6B6B).copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else if (isAuthenticated) {
                        Surface(
                            onClick = onContactar,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF0066CC).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Color(0xFF0066CC).copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF0066CC),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Contactar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
