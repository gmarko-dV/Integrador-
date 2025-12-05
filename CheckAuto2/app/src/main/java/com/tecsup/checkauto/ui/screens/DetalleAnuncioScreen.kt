package com.tecsup.checkauto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tecsup.checkauto.model.Anuncio
import com.tecsup.checkauto.service.SupabaseService
import com.tecsup.checkauto.service.ModelConverter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
fun DetalleAnuncioScreen(
    anuncioId: Long,
    onBack: () -> Unit,
    onContactar: () -> Unit = {},
    esPropietario: Boolean = false,
    isAuthenticated: Boolean = false,
    userId: String? = null
) {
    var anuncio by remember { mutableStateOf<Anuncio?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(anuncioId) {
        isLoading = true
        try {
            val anuncioSupabase = SupabaseService.getAnuncioById(anuncioId.toInt())
            if (anuncioSupabase != null) {
                val imagenesSupabase = try {
                    SupabaseService.getImagenesByAnuncioId(anuncioSupabase.id_anuncio ?: 0)
                } catch (e: Exception) {
                    emptyList()
                }
                val imagenes = imagenesSupabase.map { ModelConverter.imagenSupabaseToImagen(it) }
                anuncio = ModelConverter.anuncioSupabaseToAnuncio(anuncioSupabase, imagenes)
            } else {
                errorMessage = "Anuncio no encontrado"
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error al cargar anuncio: ${e.message}"
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Mostrar mensaje de éxito
    if (mensajeExito != null) {
        LaunchedEffect(mensajeExito) {
            kotlinx.coroutines.delay(3000)
            mensajeExito = null
        }
    }
    
    if (errorMessage != null || anuncio == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorMessage ?: "Anuncio no encontrado",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Volver")
            }
        }
        return
    }
    
    val anuncioData = anuncio!!
    val esPropietarioReal = anuncioData.idUsuario == userId
    var imagenActual by remember { mutableStateOf(0) }
    var mostrarModalContacto by remember { mutableStateOf(false) }
    var mensajeContacto by remember { mutableStateOf("") }
    var contactando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagenes = anuncioData.imagenes ?: emptyList()
    val tieneMultiplesImagenes = imagenes.size > 1
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val numberFormatter = NumberFormat.getNumberInstance(Locale("es", "PE"))

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(mensajeExito) {
        mensajeExito?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            mensajeExito = null
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            // Header moderno con gradiente
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Detalle del Vehículo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Imagen principal mejorada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
            if (imagenes.isNotEmpty() && imagenActual < imagenes.size) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imagenes[imagenActual].urlImagen)
                            .crossfade(true)
                            .allowHardware(false) // Permitir todos los formatos de imagen
                            .build()
                    ),
                    contentDescription = anuncioData.modelo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "🚗",
                            fontSize = 80.sp
                        )
                    }
                }
            }

            // Controles de navegación de imágenes
            if (tieneMultiplesImagenes) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            imagenActual = (imagenActual - 1 + imagenes.size) % imagenes.size
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Anterior",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            imagenActual = (imagenActual + 1) % imagenes.size
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .size(48.dp)
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

            // Miniaturas de imágenes
            if (tieneMultiplesImagenes && imagenes.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    itemsIndexed(imagenes.take(4)) { index, imagen ->
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { imagenActual = index }
                                .then(
                                    if (index == imagenActual) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    } else Modifier
                                )
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(imagen.urlImagen)
                                        .crossfade(true)
                                        .allowHardware(false) // Permitir todos los formatos de imagen
                                        .build()
                                ),
                                contentDescription = "Miniatura ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Información del vehículo mejorada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
            // Título y año
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.3.sp,
                        lineHeight = 34.sp
                    )
                    if (anuncioData.modelo != null) {
                        Text(
                            text = anuncioData.modelo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0066CC)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "${anuncioData.anio}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Precio destacado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatter.format(anuncioData.precio),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066CC),
                    letterSpacing = 0.5.sp
                )
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Precio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Especificaciones mejoradas
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Especificaciones",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (anuncioData.tipoVehiculo != null) {
                        SpecRow("Tipo", anuncioData.tipoVehiculo)
                    }
                    SpecRow("Kilometraje", "${numberFormatter.format(anuncioData.kilometraje)} km")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Descripción mejorada
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Descripción",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = anuncioData.descripcion,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Datos de contacto (solo si está autenticado)
            if (isAuthenticated && (anuncioData.emailContacto != null || anuncioData.telefonoContacto != null)) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Datos de Contacto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (anuncioData.emailContacto != null) {
                            SpecRow("Email", anuncioData.emailContacto)
                        }
                        if (anuncioData.telefonoContacto != null) {
                            SpecRow("Teléfono", anuncioData.telefonoContacto)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción mejorados
            if (isAuthenticated && !esPropietarioReal) {
                Button(
                    onClick = {
                        mensajeContacto = "Hola, estoy interesado en tu vehículo: ${anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}"}. Me gustaría obtener más información."
                        mostrarModalContacto = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066CC)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Contactar Vendedor",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            } else if (!isAuthenticated) {
                Text(
                    text = "Inicia sesión para contactar al vendedor",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            }
            } // Cierre del Column principal
    
            // Modal de contacto mejorado
            if (mostrarModalContacto) {
        AlertDialog(
            onDismissRequest = { mostrarModalContacto = false },
            title = { 
                Text(
                    "💬 Enviar Mensaje",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 0.5.sp
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F4F8)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = formatter.format(anuncioData.precio),
                                color = Color(0xFF0066CC),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    OutlinedTextField(
                        value = mensajeContacto,
                        onValueChange = { mensajeContacto = it },
                        label = { Text("Tu mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0066CC),
                            focusedLabelColor = Color(0xFF0066CC)
                        )
                    )
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("ℹ️", fontSize = 16.sp)
                            Text(
                                text = "El vendedor recibirá una notificación",
                                fontSize = 12.sp,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        contactando = true
                        scope.launch {
                            try {
                                val compradorId = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentUserId()
                                val compradorEmail = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentUserEmail()
                                val compradorNombre = com.tecsup.checkauto.service.SupabaseAuthService.getCurrentUser()?.userMetadata?.get("nombre")?.toString()
                                
                                if (compradorId != null && anuncioData.idUsuario != null) {
                                    // Crear notificación
                                    SupabaseService.createNotificacion(
                                        com.tecsup.checkauto.service.NotificacionSupabase(
                                            id_vendedor = anuncioData.idUsuario,
                                            id_comprador = compradorId,
                                            nombre_comprador = compradorNombre,
                                            email_comprador = compradorEmail,
                                            id_anuncio = anuncioData.idAnuncio?.toInt(),
                                            titulo = "Nuevo mensaje sobre tu vehículo",
                                            mensaje = mensajeContacto,
                                            tipo = "interes"
                                        )
                                    )
                                }
                                
                                contactando = false
                                mostrarModalContacto = false
                                mensajeContacto = "" // Limpiar el mensaje
                                mensajeExito = "¡Mensaje enviado exitosamente! El vendedor recibirá una notificación."
                            } catch (e: Exception) {
                                contactando = false
                                errorMessage = "Error al enviar mensaje: ${e.message}"
                            }
                        }
                    },
                    enabled = mensajeContacto.isNotBlank() && !contactando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066CC)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (contactando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Enviando...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "📤 Enviar Mensaje",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarModalContacto = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text(
                        "Cancelar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
        } // Cierre del Box
    } // Cierre del contenido del Scaffold
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFF0066CC).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0066CC),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

