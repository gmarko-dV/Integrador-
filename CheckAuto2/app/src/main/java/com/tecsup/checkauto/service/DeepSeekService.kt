package com.tecsup.checkauto.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.Locale
import android.util.Log

object DeepSeekService {
    private const val API_KEY = "sk-e3d1beec423643f094bbb65a4c653ff0"
    private const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true  // Incluir valores por defecto para asegurar que 'model' se envíe
            })
        }
    }
    
    @Serializable
    data class Message(
        val role: String,
        val content: String
    )
    
    @Serializable
    data class ChatRequest(
        val model: String = "deepseek-chat",
        val messages: List<Message>,
        val temperature: Double = 0.7,
        val max_tokens: Int = 2000
    )
    
    @Serializable
    data class ChatResponse(
        val choices: List<Choice>? = null,
        val error: ErrorResponse? = null
    )
    
    @Serializable
    data class Choice(
        val message: Message? = null,
        val delta: Message? = null
    )
    
    @Serializable
    data class ErrorResponse(
        val message: String? = null,
        val type: String? = null,
        val code: String? = null
    )
    
    /**
     * Enviar mensaje a DeepSeek API con contexto sobre vehículos disponibles
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Message> = emptyList(),
        vehiculosContext: String = ""
    ): String {
        try {
            // Construir el mensaje del sistema con contexto
            val systemMessage = """
                Eres un asistente virtual especializado en ayudar a usuarios a encontrar vehículos en CheckAuto, 
                una plataforma de compra y venta de autos en Perú. 
                
                Tu función es:
                - Ayudar a los usuarios a encontrar vehículos según sus necesidades
                - Proporcionar información sobre precios, características y disponibilidad
                - Responder preguntas sobre tipos de vehículos (SUV, Sedán, Hatchback, etc.)
                - Dar consejos sobre qué buscar al comprar un vehículo usado
                
                ${if (vehiculosContext.isNotEmpty()) "Información actual de vehículos disponibles:\n$vehiculosContext" else ""}
                
                Responde de manera amigable, profesional y en español. Si no tienes información específica 
                sobre un vehículo, guía al usuario sobre cómo buscarlo en la plataforma.
            """.trimIndent()
            
            // Construir lista de mensajes
            val messages = mutableListOf<Message>()
            messages.add(Message("system", systemMessage))
            messages.addAll(conversationHistory)
            messages.add(Message("user", userMessage))
            
            val request = ChatRequest(
                model = "deepseek-chat",
                messages = messages,
                temperature = 0.7,
                max_tokens = 2000
            )
            
            // Log del request para debugging
            val requestJson = Json { encodeDefaults = true }.encodeToString(request)
            Log.d("DeepSeekService", "Request JSON: $requestJson")
            
            val httpResponse = client.post(API_URL) {
                header(HttpHeaders.Authorization, "Bearer $API_KEY")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }
            
            // Verificar el código de estado HTTP
            if (!httpResponse.status.isSuccess()) {
                val errorBody = httpResponse.bodyAsText()
                Log.e("DeepSeekService", "Error HTTP ${httpResponse.status.value}: $errorBody")
                return "Error al conectar con el asistente (${httpResponse.status.value}). Por favor, intenta nuevamente."
            }
            
            // Leer la respuesta como texto primero para debugging
            val responseText = httpResponse.bodyAsText()
            Log.d("DeepSeekService", "Respuesta de DeepSeek: $responseText")
            
            // Intentar deserializar la respuesta
            val response = try {
                Json { 
                    ignoreUnknownKeys = true
                    isLenient = true
                }.decodeFromString<ChatResponse>(responseText)
            } catch (e: Exception) {
                Log.e("DeepSeekService", "Error al deserializar respuesta: ${e.message}")
                Log.e("DeepSeekService", "Respuesta recibida: $responseText")
                return "Error al procesar la respuesta del asistente. Por favor, intenta nuevamente."
            }
            
            // Verificar si hay un error en la respuesta
            if (response.error != null) {
                val errorMsg = response.error.message ?: "Error desconocido"
                Log.e("DeepSeekService", "Error de API: $errorMsg")
                return "Error del asistente: $errorMsg"
            }
            
            // Extraer el contenido de la respuesta
            val content = response.choices?.firstOrNull()?.message?.content
                ?: response.choices?.firstOrNull()?.delta?.content
            
            return content ?: "Lo siento, no pude procesar tu consulta. Por favor, intenta nuevamente."
        } catch (e: Exception) {
            Log.e("DeepSeekService", "Excepción al enviar mensaje: ${e.message}", e)
            e.printStackTrace()
            return "Error al conectar con el asistente. Por favor, verifica tu conexión e intenta nuevamente."
        }
    }
    
    /**
     * Obtener contexto de vehículos disponibles para enriquecer las respuestas de la IA
     */
    suspend fun getVehiculosContext(): String {
        return try {
            val anuncios = SupabaseService.getAnuncios()
            if (anuncios.isEmpty()) {
                "Actualmente no hay vehículos disponibles en la plataforma."
            } else {
                val tipos: List<String> = anuncios.mapNotNull { anuncio -> anuncio.tipo_vehiculo }.distinct()
                val precios: List<Double> = anuncios.map { anuncio -> anuncio.precio }
                val precioMin = precios.minOrNull() ?: 0.0
                val precioMax = precios.maxOrNull() ?: 0.0
                val precioPromedio = precios.average()
                
                """
                Hay ${anuncios.size} vehículos disponibles en la plataforma.
                Tipos de vehículos disponibles: ${tipos.joinToString(", ")}
                Rango de precios: S/ ${String.format(Locale.getDefault(), "%.0f", precioMin)} - S/ ${String.format(Locale.getDefault(), "%.0f", precioMax)}
                Precio promedio: S/ ${String.format(Locale.getDefault(), "%.0f", precioPromedio)}
                """.trimIndent()
            }
        } catch (_: Exception) {
            ""
        }
    }
}

