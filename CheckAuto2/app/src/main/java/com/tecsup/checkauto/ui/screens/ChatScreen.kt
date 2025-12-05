package com.tecsup.checkauto.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.checkauto.model.Conversacion
import com.tecsup.checkauto.model.Mensaje
import com.tecsup.checkauto.service.SupabaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    idConversacion: Long,
    onBack: () -> Unit,
    isAuthenticated: Boolean = false,
    userId: String? = null
) {
    var conversacion by remember { mutableStateOf<Conversacion?>(null) }
    var mensajes by remember { mutableStateOf<List<Mensaje>>(emptyList()) }
    var nuevoMensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Función para cargar mensajes
    fun cargarMensajes(idConv: Int) {
        scope.launch {
            try {
                val mensajesSupabase = SupabaseService.getMensajesByConversacion(idConv)
                mensajes = mensajesSupabase.map { msg ->
                    Mensaje(
                        idMensaje = msg.id_mensaje?.toLong(),
                        idConversacion = msg.id_conversacion.toLong(),
                        idRemitente = msg.id_remitente,
                        mensaje = msg.mensaje,
                        leido = msg.leido,
                        fechaEnvio = msg.fecha_envio
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error al cargar mensajes: ${e.message}", e)
            }
        }
    }

    // Cargar conversación y mensajes
    LaunchedEffect(idConversacion) {
        if (userId != null && isAuthenticated) {
            isLoading = true
            try {
                val convSupabase = SupabaseService.getConversacionById(idConversacion.toInt())
                if (convSupabase != null) {
                    // Verificar acceso
                    if (convSupabase.id_vendedor != userId && convSupabase.id_comprador != userId) {
                        errorMessage = "No tienes acceso a esta conversación"
                        isLoading = false
                        return@LaunchedEffect
                    }

                    conversacion = Conversacion(
                        idConversacion = convSupabase.id_conversacion?.toLong(),
                        idAnuncio = convSupabase.id_anuncio.toLong(),
                        idVendedor = convSupabase.id_vendedor,
                        idComprador = convSupabase.id_comprador,
                        fechaCreacion = convSupabase.fecha_creacion,
                        fechaUltimoMensaje = convSupabase.fecha_ultimo_mensaje,
                        activa = convSupabase.activa
                    )

                    // Cargar mensajes
                    cargarMensajes(idConversacion.toInt())
                } else {
                    errorMessage = "Conversación no encontrada"
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error al cargar conversación: ${e.message}", e)
                errorMessage = "Error al cargar la conversación: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Actualizar mensajes periódicamente
    LaunchedEffect(idConversacion) {
        while (true) {
            delay(5000) // Actualizar cada 5 segundos
            if (userId != null && isAuthenticated) {
                try {
                    cargarMensajes(idConversacion.toInt())
                    // Marcar como leídos
                    SupabaseService.marcarMensajesComoLeidos(idConversacion.toInt(), userId)
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error al actualizar mensajes: ${e.message}", e)
                }
            }
        }
    }

    // Scroll al final cuando hay nuevos mensajes
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    fun enviarMensaje() {
        if (nuevoMensaje.trim().isEmpty() || enviando || userId == null) return

        scope.launch {
            try {
                enviando = true
                val mensajeSupabase = com.tecsup.checkauto.service.MensajeSupabase(
                    id_mensaje = null,
                    id_conversacion = idConversacion.toInt(),
                    id_remitente = userId,
                    mensaje = nuevoMensaje.trim(),
                    leido = false,
                    fecha_envio = null
                )

                SupabaseService.enviarMensaje(mensajeSupabase)
                nuevoMensaje = ""
                
                // Recargar mensajes
                cargarMensajes(idConversacion.toInt())
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error al enviar mensaje: ${e.message}", e)
                errorMessage = "Error al enviar el mensaje: ${e.message}"
            } finally {
                enviando = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Anuncio #${conversacion?.idAnuncio ?: ""}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            if (conversacion?.idVendedor == userId) "Comprador" else "Vendedor",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                            Button(onClick = onBack) {
                                Text("Volver")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Lista de mensajes
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mensajes) { mensaje ->
                                MensajeBurbuja(
                                    mensaje = mensaje,
                                    esMiMensaje = mensaje.idRemitente == userId
                                )
                            }
                        }

                        // Input de mensaje
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF1A1F2E).copy(alpha = 0.8f),
                                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nuevoMensaje,
                                onValueChange = { nuevoMensaje = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { 
                                    Text(
                                        "Escribe un mensaje...", 
                                        color = Color.White.copy(alpha = 0.6f)
                                    ) 
                                },
                                enabled = !enviando,
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF0066CC),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                            IconButton(
                                onClick = { enviarMensaje() },
                                enabled = nuevoMensaje.trim().isNotEmpty() && !enviando
                            ) {
                                if (enviando) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar",
                                        tint = if (nuevoMensaje.trim().isNotEmpty()) 
                                            Color(0xFF0066CC) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MensajeBurbuja(
    mensaje: Mensaje,
    esMiMensaje: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esMiMensaje) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (esMiMensaje) 
                    Color(0xFF0066CC) 
                else 
                    Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = mensaje.mensaje,
                    color = if (esMiMensaje) Color.White else Color(0xFF1A1F2E),
                    fontSize = 14.sp
                )
                Text(
                    text = formatearHoraMensaje(mensaje.fechaEnvio),
                    fontSize = 10.sp,
                    color = if (esMiMensaje) 
                        Color.White.copy(alpha = 0.7f) 
                    else 
                        Color(0xFF1A1F2E).copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatearHoraMensaje(fecha: String?): String {
    if (fecha == null) return ""
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = formato.parse(fecha) ?: return fecha
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        fecha
    }
}

