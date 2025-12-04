package com.tecsup.checkauto.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.tecsup.checkauto.R
import com.tecsup.checkauto.service.DeepSeekService
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class ChatMessage(
    val role: String, // "user" o "assistant"
    val content: String
)

@Composable
fun FloatingChatBubble() {
    var isExpanded by remember { mutableStateOf(false) }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "assistant",
                    "¡Hola! 👋 Soy tu asistente virtual de CheckAuto. Puedo ayudarte a encontrar el vehículo perfecto. ¿Qué tipo de auto buscas o qué información necesitas?"
                )
            )
        )
    }
    var inputMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Burbuja flotante (overlay que no bloquea el contenido)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Botón flotante
        FloatingActionButton(
            onClick = { isExpanded = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            // Logo de la IA
            // INSTRUCCIONES: 
            // 1. Copia "LOGO IA.png" desde Descargas
            // 2. Renómbralo a "logo_ia.png" (minúsculas, guión bajo)
            // 3. Colócalo en: CheckAuto2/app/src/main/res/drawable/logo_ia.png
            // 4. Descomenta las líneas de Image y comenta las de Icon
            
            // Logo del chatbot
            Image(
                painter = painterResource(id = R.drawable.chatbot_movile),
                contentDescription = "Chat IA",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                contentScale = ContentScale.Crop
            )
        }
        
        // Chat expandido
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut()
        ) {
            Dialog(
                onDismissRequest = { isExpanded = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
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
                        // Header con gradiente azul
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
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.chatbot_movile),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = "Asistente CheckAuto",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { isExpanded = false }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        
                        // Mensajes
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                ChatMessageBubble(message = message)
                            }
                            
                            if (isLoading) {
                                item {
                                    ChatMessageBubble(
                                        message = ChatMessage("assistant", "Escribiendo...")
                                    )
                                }
                            }
                        }
                        
                        // Input con glassmorphism mejorado
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputMessage,
                                    onValueChange = { inputMessage = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Escribe tu mensaje...", color = Color.White.copy(alpha = 0.5f)) },
                                    maxLines = 3,
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                        focusedBorderColor = Color(0xFF0066CC),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedLabelColor = Color(0xFF0066CC),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                                    ),
                                    trailingIcon = {
                                        if (inputMessage.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    inputMessage = ""
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Limpiar",
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                                FloatingActionButton(
                                    onClick = {
                                        if (inputMessage.isNotBlank() && !isLoading) {
                                            val userMessage = inputMessage.trim()
                                            inputMessage = ""
                                            messages = messages + ChatMessage("user", userMessage)
                                            isLoading = true
                                            
                                            scope.launch {
                                                try {
                                                    // Obtener contexto de vehículos
                                                    val vehiculosContext = DeepSeekService.getVehiculosContext()
                                                    
                                                    // Construir historial de conversación
                                                    val conversationHistory = messages
                                                        .drop(1) // Remover mensaje inicial
                                                        .map { 
                                                            DeepSeekService.Message(
                                                                it.role,
                                                                it.content
                                                            )
                                                        }
                                                    
                                                    // Enviar mensaje a DeepSeek
                                                    val response = DeepSeekService.sendMessage(
                                                        userMessage = userMessage,
                                                        conversationHistory = conversationHistory,
                                                        vehiculosContext = vehiculosContext
                                                    )
                                                    
                                                    messages = messages + ChatMessage("assistant", response)
                                                } catch (_: Exception) {
                                                    messages = messages + ChatMessage(
                                                        "assistant",
                                                        "Lo siento, ocurrió un error al procesar tu consulta. Por favor, intenta nuevamente."
                                                    )
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp),
                                    containerColor = Color(0xFF0066CC),
                                    shape = CircleShape
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Enviar",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
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
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            color = if (isUser) 
                Color(0xFF0066CC).copy(alpha = 0.9f)
            else 
                Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser)
                    Color(0xFF0066CC).copy(alpha = 0.5f)
                else
                    Color.White.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (!isUser) {
                    Surface(
                        color = Color(0xFF0066CC).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chatbot_movile),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Text(
                    text = message.content,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

