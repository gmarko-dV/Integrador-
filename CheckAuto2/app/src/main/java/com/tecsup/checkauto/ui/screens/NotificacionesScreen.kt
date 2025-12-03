package com.tecsup.checkauto.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.tecsup.checkauto.service.SupabaseService
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class Notificacion(
    val idNotificacion: Long,
    val titulo: String? = null,
    val mensaje: String,
    val nombreComprador: String? = null,
    val emailComprador: String? = null,
    val fechaCreacion: String,
    val leida: Boolean = false,
    val idAnuncio: Long? = null
)

@Composable
fun NotificacionesScreen(
    vendedorId: String? = null,
    onBack: () -> Unit = {},
    onAnuncioClick: (Long) -> Unit = {}
) {
    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Función para cargar notificaciones
    fun cargarNotificaciones() {
        if (vendedorId == null) {
            isLoading = false
            return
        }
        
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val notificacionesSupabase = SupabaseService.getNotificacionesByVendedor(vendedorId)
                notificaciones = notificacionesSupabase.map { notif ->
                    Notificacion(
                        idNotificacion = notif.id_notificacion?.toLong() ?: 0L,
                        titulo = notif.titulo,
                        mensaje = notif.mensaje ?: "",
                        nombreComprador = notif.nombre_comprador,
                        emailComprador = notif.email_comprador,
                        fechaCreacion = notif.fecha_creacion ?: "",
                        leida = notif.leido || notif.leida,
                        idAnuncio = notif.id_anuncio?.toLong()
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error al cargar notificaciones: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Cargar notificaciones al iniciar y cuando cambia el vendedorId
    LaunchedEffect(vendedorId) {
        cargarNotificaciones()
    }

    val cantidadNoLeidas = notificaciones.count { !it.leida }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
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
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
                Text(
                    text = "Notificaciones",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (cantidadNoLeidas > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = cantidadNoLeidas.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (notificaciones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔔",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes notificaciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón para marcar todas como leídas
                if (cantidadNoLeidas > 0) {
                    item {
                        Button(
                            onClick = {
                                if (vendedorId != null) {
                                    scope.launch {
                                        try {
                                            SupabaseService.markAllNotificacionesAsRead(vendedorId)
                                            // Recargar notificaciones
                                            val notificacionesSupabase = SupabaseService.getNotificacionesByVendedor(vendedorId)
                                            notificaciones = notificacionesSupabase.map { notif ->
                                                Notificacion(
                                                    idNotificacion = notif.id_notificacion?.toLong() ?: 0L,
                                                    titulo = notif.titulo,
                                                    mensaje = notif.mensaje ?: "",
                                                    nombreComprador = notif.nombre_comprador,
                                                    emailComprador = notif.email_comprador,
                                                    fechaCreacion = notif.fecha_creacion ?: "",
                                                    leida = notif.leido || notif.leida,
                                                    idAnuncio = notif.id_anuncio?.toLong()
                                                )
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error: ${e.message}"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Marcar todas como leídas")
                        }
                    }
                }

                items(notificaciones) { notificacion ->
                    NotificacionCard(
                        notificacion = notificacion,
                        onMarkAsRead = {
                            scope.launch {
                                try {
                                    notificacion.idNotificacion.toInt().let { id ->
                                        SupabaseService.markNotificacionAsRead(id)
                                        // Actualizar estado local
                                        notificaciones = notificaciones.map { n ->
                                            if (n.idNotificacion == notificacion.idNotificacion) {
                                                n.copy(leida = true)
                                            } else n
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                }
                            }
                        },
                        onAnuncioClick = {
                            notificacion.idAnuncio?.let { onAnuncioClick(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificacionCard(
    notificacion: Notificacion,
    onMarkAsRead: () -> Unit,
    onAnuncioClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!notificacion.leida) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notificacion.leida) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (notificacion.titulo != null) {
                        Text(
                            text = notificacion.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (notificacion.nombreComprador != null || notificacion.emailComprador != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "De: ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = notificacion.nombreComprador ?: notificacion.emailComprador ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (notificacion.nombreComprador != null && notificacion.emailComprador != null) {
                                Text(
                                    text = " (${notificacion.emailComprador})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Text(
                        text = notificacion.mensaje,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = formatFecha(notificacion.fechaCreacion),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (!notificacion.leida) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Marcar como leída",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (notificacion.idAnuncio != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onAnuncioClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver anuncio")
                }
            }
        }
    }
}

fun formatFecha(fecha: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "PE"))
        val date = inputFormat.parse(fecha)
        date?.let { outputFormat.format(it) } ?: fecha
    } catch (e: Exception) {
        fecha
    }
}

