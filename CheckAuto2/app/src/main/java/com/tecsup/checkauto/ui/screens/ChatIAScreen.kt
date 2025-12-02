package com.tecsup.checkauto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user" o "assistant"
    val content: String
)

@Composable
fun ChatIAScreen() {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "assistant",
                    "¡Hola! 👋 Soy tu asistente virtual para ayudarte a encontrar el vehículo perfecto. ¿Qué características buscas en un auto? Por ejemplo, puedes decirme el tipo de vehículo (SUV, Sedán, Hatchback, etc.), el año, el precio máximo, o el kilometraje máximo que te interesa."
                )
            )
        )
    }
    var inputMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "🤖 Asistente Virtual de Vehículos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Pregúntame sobre las características que buscas y te ayudaré a encontrar el auto perfecto",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu mensaje aquí...") },
                maxLines = 4,
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp)
            )
            FloatingActionButton(
                onClick = {
                    if (inputMessage.isNotBlank() && !isLoading) {
                        val userMessage = inputMessage.trim()
                        inputMessage = ""
                        messages = messages + ChatMessage("user", userMessage)
                        isLoading = true

                        // Simular respuesta de la IA (más adelante será una llamada real a la API)
                        // Por ahora, respuesta simulada
                        scope.launch {
                            delay(1500)
                            val response = when {
                                userMessage.contains("SUV", ignoreCase = true) -> 
                                    "Te recomiendo buscar vehículos tipo SUV. ¿Te gustaría ver algunos anuncios disponibles?"
                                userMessage.contains("precio", ignoreCase = true) -> 
                                    "¿Cuál es tu presupuesto máximo? Puedo ayudarte a filtrar vehículos según tu rango de precio."
                                else -> 
                                    "Entiendo que buscas: $userMessage. Déjame ayudarte a encontrar vehículos que se ajusten a tus necesidades. ¿Podrías darme más detalles sobre el tipo de vehículo, año o precio que buscas?"
                            }
                            messages = messages + ChatMessage("assistant", response)
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

