package com.tecsup.checkauto.ui.screens

import android.util.Log
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
import com.tecsup.checkauto.model.Conversacion
import com.tecsup.checkauto.service.SupabaseService
import com.tecsup.checkauto.service.SupabaseAuthService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaConversacionesScreen(
    onConversacionClick: (Long) -> Unit = {},
    isAuthenticated: Boolean = false,
    userId: String? = null
) {
    var conversaciones by remember { mutableStateOf<List<Conversacion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mensajesNoLeidos by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        if (userId != null && isAuthenticated) {
            isLoading = true
            errorMessage = null
            try {
                val conversacionesSupabase = SupabaseService.getConversacionesByUsuario(userId)
                conversaciones = conversacionesSupabase.map { conv ->
                    val mensajes = try {
                        SupabaseService.getMensajesByConversacion(conv.id_conversacion?.toInt() ?: 0)
                            .map { msg ->
                                com.tecsup.checkauto.model.Mensaje(
                                    idMensaje = msg.id_mensaje?.toLong(),
                                    idConversacion = msg.id_conversacion.toLong(),
                                    idRemitente = msg.id_remitente,
                                    mensaje = msg.mensaje,
                                    leido = msg.leido,
                                    fechaEnvio = msg.fecha_envio
                                )
                            }
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    Conversacion(
                        idConversacion = conv.id_conversacion?.toLong(),
                        idAnuncio = conv.id_anuncio.toLong(),
                        idVendedor = conv.id_vendedor,
                        idComprador = conv.id_comprador,
                        fechaCreacion = conv.fecha_creacion,
                        fechaUltimoMensaje = conv.fecha_ultimo_mensaje,
                        activa = conv.activa,
                        mensajes = mensajes
                    )
                }
                
                // Contar mensajes no leídos
                mensajesNoLeidos = SupabaseService.contarMensajesNoLeidos(userId)
            } catch (e: Exception) {
                Log.e("ListaConversaciones", "Error al cargar conversaciones: ${e.message}", e)
                errorMessage = "Error al cargar las conversaciones: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Conversaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (mensajesNoLeidos > 0) {
                            Badge(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    mensajesNoLeidos.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0066CC)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1F2E), // Azul muy oscuro que complementa el header
                            Color(0xFF0F1419)  // Negro azulado más suave
                        )
                    )
                )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                errorMessage ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        try {
                                            val conversacionesSupabase = SupabaseService.getConversacionesByUsuario(userId ?: "")
                                            conversaciones = conversacionesSupabase.map { conv ->
                                                Conversacion(
                                                    idConversacion = conv.id_conversacion?.toLong(),
                                                    idAnuncio = conv.id_anuncio.toLong(),
                                                    idVendedor = conv.id_vendedor,
                                                    idComprador = conv.id_comprador,
                                                    fechaCreacion = conv.fecha_creacion,
                                                    fechaUltimoMensaje = conv.fecha_ultimo_mensaje,
                                                    activa = conv.activa
                                                )
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                conversaciones.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                "No tienes conversaciones",
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                "Cuando contactes a un vendedor, aparecerán aquí",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(conversaciones) { conversacion ->
                            ConversacionCard(
                                conversacion = conversacion,
                                userId = userId,
                                onClick = {
                                    conversacion.idConversacion?.let { id ->
                                        onConversacionClick(id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversacionCard(
    conversacion: Conversacion,
    userId: String?,
    onClick: () -> Unit
) {
    val esVendedor = conversacion.idVendedor == userId
    val otroUsuario = if (esVendedor) "Comprador" else "Vendedor"
    val ultimoMensaje = conversacion.mensajes?.lastOrNull()
    val noLeidos = conversacion.mensajes?.count { 
        !it.leido && it.idRemitente != userId 
    } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Anuncio #${conversacion.idAnuncio}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    if (noLeidos > 0) {
                        Badge {
                            Text(
                                noLeidos.toString(),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    otroUsuario,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                ultimoMensaje?.let { mensaje ->
                    Text(
                        mensaje.mensaje,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                conversacion.fechaUltimoMensaje?.let { fecha ->
                    Text(
                        formatearFecha(fecha),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun formatearFecha(fecha: String): String {
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = formato.parse(fecha) ?: return fecha
        val ahora = Date()
        val diff = ahora.time - date.time
        val minutos = diff / 60000
        val horas = diff / 3600000
        val dias = diff / 86400000

        when {
            minutos < 1 -> "Ahora"
            minutos < 60 -> "Hace ${minutos}min"
            horas < 24 -> "Hace ${horas}h"
            dias < 7 -> "Hace ${dias}d"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        fecha
    }
}

