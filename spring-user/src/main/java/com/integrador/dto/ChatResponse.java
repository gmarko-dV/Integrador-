package com.integrador.dto;

import java.util.List;

public class ChatResponse {
    private String response;
    private List<Long> recommendedAnuncioIds;
    private boolean hasRecommendations;
    
    public ChatResponse() {}
    
    public ChatResponse(String response, List<Long> recommendedAnuncioIds) {
        this.response = response;
        this.recommendedAnuncioIds = recommendedAnuncioIds;
        this.hasRecommendations = recommendedAnuncioIds != null && !recommendedAnuncioIds.isEmpty();
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public List<Long> getRecommendedAnuncioIds() {
        return recommendedAnuncioIds;
    }
    
    public void setRecommendedAnuncioIds(List<Long> recommendedAnuncioIds) {
        this.recommendedAnuncioIds = recommendedAnuncioIds;
        this.hasRecommendations = recommendedAnuncioIds != null && !recommendedAnuncioIds.isEmpty();
    }
    
    public boolean isHasRecommendations() {
        return hasRecommendations;
    }
    
    public void setHasRecommendations(boolean hasRecommendations) {
        this.hasRecommendations = hasRecommendations;
    }
}

