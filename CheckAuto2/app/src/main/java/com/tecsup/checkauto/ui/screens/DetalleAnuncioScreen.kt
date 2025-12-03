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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            // Header con botón de volver
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Detalle del Vehículo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Imagen principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
            if (imagenes.isNotEmpty() && imagenActual < imagenes.size) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imagenes[imagenActual].urlImagen)
                            .crossfade(true)
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

            // Información del vehículo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            // Título y año
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${anuncioData.anio}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Precio
            Text(
                text = formatter.format(anuncioData.precio),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Especificaciones
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Especificaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (anuncioData.tipoVehiculo != null) {
                SpecRow("Tipo", anuncioData.tipoVehiculo)
            }
            SpecRow("Kilometraje", "${numberFormatter.format(anuncioData.kilometraje)} km")

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Descripción
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Descripción",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = anuncioData.descripcion,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            // Datos de contacto (solo si está autenticado)
            if (isAuthenticated && (anuncioData.emailContacto != null || anuncioData.telefonoContacto != null)) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Datos de Contacto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (anuncioData.emailContacto != null) {
                    SpecRow("Email", anuncioData.emailContacto)
                }
                if (anuncioData.telefonoContacto != null) {
                    SpecRow("Teléfono", anuncioData.telefonoContacto)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            if (isAuthenticated && !esPropietarioReal) {
                Button(
                    onClick = {
                        mensajeContacto = "Hola, estoy interesado en tu vehículo: ${anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}"}. Me gustaría obtener más información."
                        mostrarModalContacto = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contactar Vendedor", fontSize = 16.sp)
                }
            } else if (!isAuthenticated) {
                Text(
                    text = "Inicia sesión para contactar al vendedor",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            }
            } // Cierre del Column principal
    
            // Modal de contacto (fuera del Column pero dentro del Box)
            if (mostrarModalContacto) {
        AlertDialog(
            onDismissRequest = { mostrarModalContacto = false },
            title = { Text("💬 Enviar Mensaje al Vendedor") },
            text = {
                Column {
                    Text(
                        text = anuncioData.titulo ?: "${anuncioData.modelo} ${anuncioData.anio}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formatter.format(anuncioData.precio),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = mensajeContacto,
                        onValueChange = { mensajeContacto = it },
                        label = { Text("Tu mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6
                    )
                    Text(
                        text = "El vendedor recibirá una notificación con tu mensaje",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
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
                    enabled = mensajeContacto.isNotBlank() && !contactando
                ) {
                    if (contactando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...")
                    } else {
                        Text("📤 Enviar Mensaje")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalContacto = false }) {
                    Text("Cancelar")
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

