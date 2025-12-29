package com.example.demo.mapper;

import com.example.demo.dto.Entity;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.Suggestion;
import com.example.demo.dto.SummaryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class NlpCloudMapper {

    private final ObjectMapper objectMapper;

    public NlpCloudMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GrammarResponse toGrammarResponse(String payload) {
        JsonNode root = parse(payload);
        String correctedText = root.path("corrected_text").asText("");
        List<Suggestion> suggestions = new ArrayList<>();
        JsonNode suggestionsNode = root.has("suggestions") ? root.path("suggestions") : root.path("corrections");
        for (JsonNode node : suggestionsNode) {
            suggestions.add(new Suggestion(
                    node.path("type").asText(""),
                    node.path("text").asText(""),
                    node.path("suggestion").asText(""),
                    node.path("start").asInt(0),
                    node.path("end").asInt(0)
            ));
        }
        return new GrammarResponse(correctedText, suggestions);
    }

    public EntityExtractionResponse toEntityExtractionResponse(String payload) {
        JsonNode root = parse(payload);
        List<Entity> entities = new ArrayList<>();
        for (JsonNode node : root.path("entities")) {
            entities.add(new Entity(
                    node.path("entity").asText(node.path("type").asText("")),
                    node.path("text").asText(""),
                    node.path("start").asInt(0),
                    node.path("end").asInt(0),
                    node.path("confidence").asDouble(node.path("score").asDouble(0.0))
            ));
        }
        return new EntityExtractionResponse(entities);
    }

    public SummaryResponse toSummaryResponse(String payload) {
        JsonNode root = parse(payload);
        String summary = root.path("summary_text").asText(root.path("summary").asText(""));
        List<String> keyFindings = StreamSupport.stream(root.path("key_findings").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());
        return new SummaryResponse(summary, keyFindings);
    }

    public KeywordResponse toKeywordResponse(String payload) {
        JsonNode root = parse(payload);
        List<String> keywords = StreamSupport.stream(root.path("keywords").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());
        return new KeywordResponse(keywords);
    }

    private JsonNode parse(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse NLP Cloud payload", e);
        }
    }
}
