package com.tecsup.checkauto.ui.screens

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
        // Header mejorado con gradiente
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
                    text = "Notificaciones",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                if (cantidadNoLeidas > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFDC2626)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = if (cantidadNoLeidas > 99) "99+" else cantidadNoLeidas.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDC2626).copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("❌", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Error",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626).copy(alpha = 0.9f)
                        )
                    }
                }
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        color = Color(0xFFF0F4F8),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "🔔",
                                fontSize = 64.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No tienes notificaciones",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cuando recibas mensajes sobre tus anuncios, aparecerán aquí",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón para marcar todas como leídas mejorado
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Marcar todas como leídas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (notificacion.idAnuncio != null) onAnuncioClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (!notificacion.leida) 
                Color(0xFFE0F2FE)
            else 
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notificacion.leida) 8.dp else 4.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Indicador de no leída
                if (!notificacion.leida) {
                    Surface(
                        color = Color(0xFF0066CC),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(12.dp)
                    ) {}
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (notificacion.titulo != null) {
                        Text(
                            text = notificacion.titulo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = 0.2.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (notificacion.nombreComprador != null || notificacion.emailComprador != null) {
                        Surface(
                            color = Color(0xFFF0F4F8),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF0066CC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = notificacion.nombreComprador ?: notificacion.emailComprador ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4A5568)
                                )
                                if (notificacion.nombreComprador != null && notificacion.emailComprador != null) {
                                    Text(
                                        text = "• ${notificacion.emailComprador}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = notificacion.mensaje,
                        fontSize = 15.sp,
                        color = Color(0xFF4A5568),
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatFecha(notificacion.fechaCreacion),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                if (!notificacion.leida) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0066CC).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Marcar como leída",
                            tint = Color(0xFF0066CC),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (notificacion.idAnuncio != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAnuncioClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0066CC)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0066CC))
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Ver anuncio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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

