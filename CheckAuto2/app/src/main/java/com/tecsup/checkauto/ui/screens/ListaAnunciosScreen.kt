package com.tecsup.checkauto.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tecsup.checkauto.model.Anuncio
import java.text.NumberFormat
import java.util.*

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
    // Datos de ejemplo (más adelante vendrán de la API)
    val anunciosEjemplo = remember {
        listOf(
            Anuncio(
                idAnuncio = 1,
                modelo = "Toyota Corolla",
                anio = 2020,
                kilometraje = 50000,
                precio = 35000.0,
                descripcion = "Excelente estado, único dueño, mantenimiento al día",
                emailContacto = "vendedor@ejemplo.com",
                telefonoContacto = "+51 987 654 321",
                fechaCreacion = "2024-01-15",
                imagenes = listOf(),
                tipoVehiculo = "Sedan"
            ),
            Anuncio(
                idAnuncio = 2,
                modelo = "Honda Civic",
                anio = 2019,
                kilometraje = 60000,
                precio = 32000.0,
                descripcion = "Vehículo en perfectas condiciones, revisado",
                emailContacto = "contacto@ejemplo.com",
                telefonoContacto = "+51 987 654 322",
                fechaCreacion = "2024-01-20",
                imagenes = listOf(),
                tipoVehiculo = "Sedan"
            ),
            Anuncio(
                idAnuncio = 3,
                modelo = "Toyota RAV4",
                anio = 2021,
                kilometraje = 30000,
                precio = 45000.0,
                descripcion = "SUV en excelente estado, ideal para familia",
                emailContacto = "venta@ejemplo.com",
                telefonoContacto = "+51 987 654 323",
                fechaCreacion = "2024-01-25",
                imagenes = listOf(),
                tipoVehiculo = "SUV"
            )
        )
    }

    var anuncios by remember { mutableStateOf(anunciosEjemplo) }
    var isLoading by remember { mutableStateOf(false) }
    var imagenActual by remember { mutableStateOf(mapOf<Long, Int>()) }
    var tipoFiltro by remember { mutableStateOf(tipoVehiculo) }

    // Filtrar anuncios por tipo si se especifica
    val anunciosFiltrados = remember(anuncios, tipoFiltro, esMisAnuncios, userId) {
        anuncios.filter { anuncio ->
            val cumpleTipo = tipoFiltro == null || anuncio.tipoVehiculo?.equals(tipoFiltro, ignoreCase = true) == true
            val esMio = if (esMisAnuncios) anuncio.idUsuario == userId else true
            cumpleTipo && esMio
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (tipoFiltro != null) "Vehículos $tipoFiltro en Venta" else if (esMisAnuncios) "Tus Anuncios" else "Vehículos en Venta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${anunciosFiltrados.size} ${if (anunciosFiltrados.size == 1) "anuncio disponible" else "anuncios disponibles"}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (anunciosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚗",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (esMisAnuncios) 
                            "Aún no tienes anuncios publicados"
                        else if (tipoFiltro != null) 
                            "No hay anuncios disponibles para $tipoFiltro en este momento."
                        else 
                            "No hay anuncios disponibles en este momento.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(anunciosFiltrados) { anuncio ->
                    AnuncioCard(
                        anuncio = anuncio,
                        imagenActual = imagenActual[anuncio.idAnuncio] ?: 0,
                        onImagenChange = { idAnuncio, index ->
                            imagenActual = imagenActual + (idAnuncio to index)
                        },
                        onAnuncioClick = { onAnuncioClick(anuncio.idAnuncio ?: 0) },
                        onContactar = { onContactar(anuncio) },
                        onEliminar = { onEliminar(anuncio.idAnuncio ?: 0) },
                        esMiAnuncio = esMisAnuncios && anuncio.idUsuario == userId,
                        isAuthenticated = isAuthenticated
                    )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAnuncioClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen con controles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (imagenMostrada != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data("http://localhost:8080$imagenMostrada")
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
                                fontSize = 64.sp
                            )
                        }
                    }
                }

                // Badge de tipo de vehículo
                if (anuncio.tipoVehiculo != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Título
            Text(
                text = anuncio.titulo ?: "${anuncio.modelo} ${anuncio.anio}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Precio destacado
            Text(
                text = formatter.format(anuncio.precio),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (anuncio.tipoVehiculo != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text("🚗 ", fontSize = 16.sp)
                            Text(
                                text = anuncio.tipoVehiculo,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊 ", fontSize = 16.sp)
                        Text(
                            text = "${numberFormatter.format(anuncio.kilometraje)} km",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAnuncioClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Ver Detalles")
                }
                
                if (esMiAnuncio) {
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (isAuthenticated) {
                    OutlinedButton(
                        onClick = onContactar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Contactar")
                    }
                }
            }
        }
    }
}
