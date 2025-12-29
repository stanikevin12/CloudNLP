package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.MedicalNlpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nlp")
@CrossOrigin
@Tag(name = "Medical NLP", description = "Clinical NLP utilities backed by NLP Cloud")
public class MedicalNlpController {

    private final MedicalNlpService medicalNlpService;

    public MedicalNlpController(MedicalNlpService medicalNlpService) {
        this.medicalNlpService = medicalNlpService;
    }

    @PostMapping("/grammar")
    @Operation(
            summary = "Grammar correction",
            description = "Checks grammar and spelling for the provided clinical note.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ClinicalNoteRequest.class),
                            examples = @ExampleObject(value = "{\n  \"note\": \"Pt complains of chest paiin today\",\n  \"patientContext\": \"BP 130/90, smoker\"\n}")
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grammar checked",
                            content = @Content(schema = @Schema(implementation = GrammarResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public ApiResponse<GrammarResponse> grammar(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        GrammarResponse response = medicalNlpService.checkGrammar(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/entities")
    @Operation(
            summary = "Entity extraction",
            description = "Extracts medical entities from the supplied note.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ClinicalNoteRequest.class),
                            examples = @ExampleObject(value = "{\n  \"note\": \"John Doe was admitted in Paris for chest pain\"\n}")
                    )
            ),
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Entities extracted",
                    content = @Content(schema = @Schema(implementation = EntityExtractionResponse.class)))
    )
    public ApiResponse<EntityExtractionResponse> entities(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        EntityExtractionResponse response = medicalNlpService.extractEntities(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/summarize")
    @Operation(
            summary = "Summarize clinical note",
            description = "Returns an abstractive summary of the provided note.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ClinicalNoteRequest.class),
                            examples = @ExampleObject(value = "{\n  \"note\": \"Patient presents with...\"\n}")
                    )
            ),
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note summarized",
                    content = @Content(schema = @Schema(implementation = SummaryResponse.class)))
    )
    public ApiResponse<SummaryResponse> summarize(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        SummaryResponse response = medicalNlpService.summarize(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/keywords")
    @Operation(
            summary = "Extract keywords",
            description = "Returns a list of keywords from the supplied note.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ClinicalNoteRequest.class),
                            examples = @ExampleObject(value = "{\n  \"note\": \"AI in healthcare improves diagnosis and treatment\"\n}")
                    )
            ),
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Keywords extracted",
                    content = @Content(schema = @Schema(implementation = KeywordResponse.class)))
    )
    public ApiResponse<KeywordResponse> keywords(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        KeywordResponse response = medicalNlpService.keywords(request);
        return ApiResponse.fromPayload(response, start);
    }
}
