package com.integrador.dto;

import java.util.List;

public class ChatRequest {
    private String message;
    private List<ChatMessage> conversationHistory;
    
    public ChatRequest() {}
    
    public ChatRequest(String message, List<ChatMessage> conversationHistory) {
        this.message = message;
        this.conversationHistory = conversationHistory;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<ChatMessage> getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(List<ChatMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}

