package com.integrador.controller;

import com.integrador.dto.ChatMessage;
import com.integrador.dto.ChatRequest;
import com.integrador.dto.ChatResponse;
import com.integrador.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.processMessage(
                request.getMessage(),
                request.getConversationHistory()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response.getResponse());
            result.put("recommendedAnuncioIds", response.getRecommendedAnuncioIds());
            result.put("hasRecommendations", response.isHasRecommendations());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al procesar el mensaje: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

