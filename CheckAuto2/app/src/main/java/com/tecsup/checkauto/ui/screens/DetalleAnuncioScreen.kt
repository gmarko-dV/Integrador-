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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
fun DetalleAnuncioScreen(
    anuncio: Anuncio,
    onBack: () -> Unit,
    onContactar: () -> Unit = {},
    esPropietario: Boolean = false,
    isAuthenticated: Boolean = false
) {
    var imagenActual by remember { mutableStateOf(0) }
    var mostrarModalContacto by remember { mutableStateOf(false) }
    var mensajeContacto by remember { mutableStateOf("") }
    var contactando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagenes = anuncio.imagenes ?: emptyList()
    val tieneMultiplesImagenes = imagenes.size > 1
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val numberFormatter = NumberFormat.getNumberInstance(Locale("es", "PE"))

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                            .data("http://localhost:8080${imagenes[imagenActual].urlImagen}")
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = anuncio.modelo,
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
                                    .data("http://localhost:8080${imagen.urlImagen}")
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
                    text = anuncio.titulo ?: "${anuncio.modelo} ${anuncio.anio}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${anuncio.anio}",
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
                text = formatter.format(anuncio.precio),
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

            if (anuncio.tipoVehiculo != null) {
                SpecRow("Tipo", anuncio.tipoVehiculo)
            }
            SpecRow("Kilometraje", "${numberFormatter.format(anuncio.kilometraje)} km")

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
                text = anuncio.descripcion,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            // Datos de contacto (solo si está autenticado)
            if (isAuthenticated && (anuncio.emailContacto != null || anuncio.telefonoContacto != null)) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Datos de Contacto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (anuncio.emailContacto != null) {
                    SpecRow("Email", anuncio.emailContacto)
                }
                if (anuncio.telefonoContacto != null) {
                    SpecRow("Teléfono", anuncio.telefonoContacto)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            if (isAuthenticated && !esPropietario) {
                Button(
                    onClick = {
                        mensajeContacto = "Hola, estoy interesado en tu vehículo: ${anuncio.titulo ?: "${anuncio.modelo} ${anuncio.anio}"}. Me gustaría obtener más información."
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
    }

    // Modal de contacto
    if (mostrarModalContacto) {
        AlertDialog(
            onDismissRequest = { mostrarModalContacto = false },
            title = { Text("💬 Enviar Mensaje al Vendedor") },
            text = {
                Column {
                    Text(
                        text = anuncio.titulo ?: "${anuncio.modelo} ${anuncio.anio}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formatter.format(anuncio.precio),
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
                        // Simular envío (más adelante será una llamada real a la API)
                        scope.launch {
                            delay(1000)
                            contactando = false
                            mostrarModalContacto = false
                            // Mostrar mensaje de éxito
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

