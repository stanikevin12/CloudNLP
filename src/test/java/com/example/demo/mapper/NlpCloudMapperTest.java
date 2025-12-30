package com.example.demo.mapper;

import com.example.demo.dto.Entity;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.SummaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NlpCloudMapperTest {

    private NlpCloudMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NlpCloudMapper(new ObjectMapper());
    }

    @Test
    void mapsGrammarPayload() throws IOException {
        String payload = Files.readString(Path.of("src/test/resources/fixtures/grammar.json"));

        GrammarResponse response = new GrammarResponse(mapper.readSummaryText(payload), List.of());

        assertFalse(response.getCorrectedText().isBlank());
        assertEquals("This is the corrected text.", response.getCorrectedText());
        assertTrue(response.getSuggestions().isEmpty());
    }

    @Test
    void mapsEntityExtractionPayload() throws IOException {
        String payload = Files.readString(Path.of("src/test/resources/fixtures/entities.json"));

        EntityExtractionResponse response = mapper.toEntityExtractionResponse(payload);

        List<Entity> entities = response.getEntities();
        assertEquals(2, entities.size());
        Entity first = entities.get(0);
        assertEquals("person", first.getEntity());
        assertEquals("John Doe", first.getText());
        assertEquals(0, first.getStart());
        assertEquals(8, first.getEnd());
        assertEquals(0.98, first.getConfidence(), 0.0001);
    }

    @Test
    void mapsSummaryPayload() throws IOException {
        String payload = Files.readString(Path.of("src/test/resources/fixtures/summary.json"));
 
        SummaryResponse response = mapper.toSummaryResponse(payload);

        assertEquals("This is a concise summary of the report.", response.getSummary());
        assertEquals(List.of("Finding one", "Finding two", "Finding three"), response.getKeyFindings());
    }

    @Test
    void mapsKeywordPayload() throws IOException {
        String payload = Files.readString(Path.of("src/test/resources/fixtures/keywords.json"));

        String summaryText = mapper.readSummaryText(payload);
        KeywordResponse response = new KeywordResponse(Arrays.stream(summaryText.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList());

        assertNotNull(response.getKeywords());
        assertFalse(response.getKeywords().isEmpty());
        assertTrue(response.getKeywords().contains("nlp"));
    }
}
