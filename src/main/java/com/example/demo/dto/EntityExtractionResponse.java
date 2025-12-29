package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Entity extraction output")
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

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }
}
