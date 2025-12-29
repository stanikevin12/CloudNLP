package com.example.demo.dto;

import java.util.List;

public class EntityExtractionResponse {
    private List<Entity> entities;

    public EntityExtractionResponse() {
    }

    public EntityExtractionResponse(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
