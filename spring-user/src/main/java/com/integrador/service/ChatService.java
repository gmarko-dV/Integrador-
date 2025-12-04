package com.integrador.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrador.dto.ChatMessage;
import com.integrador.dto.ChatResponse;
import com.integrador.entity.Anuncio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    @Autowired
    private AnuncioService anuncioService;
    
    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = "Eres un asistente virtual especializado en ayudar a los usuarios a encontrar el vehículo perfecto. " +
            "Tu tarea es hacer preguntas sobre las características que el usuario busca en un auto (tipo de vehículo, año, precio, kilometraje, etc.) " +
            "y luego recomendar los vehículos más adecuados de la lista disponible. " +
            "Sé amigable, profesional y específico en tus recomendaciones. " +
            "Cuando recomiendes vehículos, menciona los IDs de los anuncios recomendados al final de tu respuesta en el formato: [RECOMMEND: id1, id2, id3]";
    
    public ChatResponse processMessage(String userMessage, List<ChatMessage> conversationHistory) {
        try {
            // Obtener todos los anuncios activos
            List<Anuncio> anuncios = anuncioService.obtenerTodosLosAnunciosActivos();
            
            // Construir el contexto con los anuncios disponibles
            String anunciosContext = buildAnunciosContext(anuncios);
            
            // Construir el mensaje del sistema con el contexto
            String systemMessageWithContext = SYSTEM_PROMPT + "\n\nAnuncios disponibles:\n" + anunciosContext;
            
            // Construir la lista de mensajes para OpenAI
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemMessageWithContext));
            
            // Agregar historial de conversación
            if (conversationHistory != null) {
                for (ChatMessage msg : conversationHistory) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }
            
            // Agregar el mensaje actual del usuario
            messages.add(Map.of("role", "user", "content", userMessage));
            
            // Llamar a DeepSeek
            String aiResponse = callDeepSeek(messages);
            
            // Extraer IDs de anuncios recomendados
            List<Long> recommendedIds = extractRecommendedIds(aiResponse);
            
            // Limpiar la respuesta removiendo el formato de recomendación
            String cleanResponse = cleanResponse(aiResponse);
            
            return new ChatResponse(cleanResponse, recommendedIds);
            
        } catch (Exception e) {
            // Si falla la llamada a DeepSeek, usar un sistema de recomendación básico
            return fallbackRecommendation(userMessage);
        }
    }
    
    private String buildAnunciosContext(List<Anuncio> anuncios) {
        if (anuncios.isEmpty()) {
            return "No hay anuncios disponibles en este momento.";
        }
        
        StringBuilder context = new StringBuilder();
        for (Anuncio anuncio : anuncios) {
            context.append(String.format(
                "ID: %d | Modelo: %s | Año: %d | Precio: %s | Kilometraje: %d km | Tipo: %s | Descripción: %s\n",
                anuncio.getIdAnuncio(),
                anuncio.getModelo(),
                anuncio.getAnio(),
                anuncio.getPrecio().toString(),
                anuncio.getKilometraje(),
                anuncio.getTipoVehiculo() != null ? anuncio.getTipoVehiculo() : "No especificado",
                anuncio.getDescripcion().length() > 100 
                    ? anuncio.getDescripcion().substring(0, 100) + "..." 
                    : anuncio.getDescripcion()
            ));
        }
        return context.toString();
    }
    
    private String callDeepSeek(List<Map<String, String>> messages) throws Exception {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            throw new Exception("DeepSeek API key no configurada");
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepseekApiKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            deepseekApiUrl,
            HttpMethod.POST,
            request,
            String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new Exception("Error al llamar a DeepSeek: " + response.getStatusCode());
        }
    }
    
    private List<Long> extractRecommendedIds(String response) {
        List<Long> ids = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[RECOMMEND:\\s*([0-9,\\s]+)\\]");
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            String idsStr = matcher.group(1);
            String[] idArray = idsStr.split(",");
            for (String idStr : idArray) {
                try {
                    ids.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    // Ignorar IDs inválidos
                }
            }
        }
        
        return ids;
    }
    
    private String cleanResponse(String response) {
        // Remover el formato de recomendación de la respuesta
        return response.replaceAll("\\[RECOMMEND:[^\\]]+\\]", "").trim();
    }
    
    private ChatResponse fallbackRecommendation(String userMessage) {
        // Sistema de recomendación básico sin IA
        List<Anuncio> anuncios = anuncioService.obtenerTodosLosAnunciosActivos();
        String lowerMessage = userMessage.toLowerCase();
        List<Anuncio> filtered = new ArrayList<>();
        
        // Filtrar por tipo de vehículo
        if (lowerMessage.contains("suv") || lowerMessage.contains("s.u.v")) {
            filtered = anuncios.stream()
                .filter(a -> "suv".equalsIgnoreCase(a.getTipoVehiculo()))
                .collect(Collectors.toList());
        } else if (lowerMessage.contains("sedan") || lowerMessage.contains("sedán")) {
            filtered = anuncios.stream()
                .filter(a -> "sedan".equalsIgnoreCase(a.getTipoVehiculo()))
                .collect(Collectors.toList());
        } else if (lowerMessage.contains("hatchback")) {
            filtered = anuncios.stream()
                .filter(a -> "hatchback".equalsIgnoreCase(a.getTipoVehiculo()))
                .collect(Collectors.toList());
        } else if (lowerMessage.contains("coupe") || lowerMessage.contains("coupé")) {
            filtered = anuncios.stream()
                .filter(a -> "coupe".equalsIgnoreCase(a.getTipoVehiculo()))
                .collect(Collectors.toList());
        } else if (lowerMessage.contains("deportivo")) {
            filtered = anuncios.stream()
                .filter(a -> "deportivo".equalsIgnoreCase(a.getTipoVehiculo()))
                .collect(Collectors.toList());
        } else {
            filtered = anuncios;
        }
        
        // Limitar a los primeros 5
        List<Long> recommendedIds = filtered.stream()
            .limit(5)
            .map(Anuncio::getIdAnuncio)
            .collect(Collectors.toList());
        
        String response = filtered.isEmpty() 
            ? "Lo siento, no encontré vehículos que coincidan con tus criterios. ¿Podrías ser más específico sobre qué tipo de vehículo buscas?"
            : "Encontré " + filtered.size() + " vehículo(s) que podrían interesarte. Aquí están mis recomendaciones:";
        
        return new ChatResponse(response, recommendedIds);
    }
}

