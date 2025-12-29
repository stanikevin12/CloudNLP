package com.example.demo.controller;

import com.example.demo.dto.ApiResult;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.ErrorResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.SummaryResponse;
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
@Tag(name = "Medical NLP", description = "Clinical NLP utilities routed Controller → Service → Mapper → upstream NLP engine")
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ApiResult<GrammarResponse> grammar(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        GrammarResponse response = medicalNlpService.checkGrammar(request);
        return ApiResult.fromPayload(response, start);
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
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Entities extracted",
                            content = @Content(schema = @Schema(implementation = EntityExtractionResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ApiResult<EntityExtractionResponse> entities(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        EntityExtractionResponse response = medicalNlpService.extractEntities(request);
        return ApiResult.fromPayload(response, start);
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
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note summarized",
                            content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ApiResult<SummaryResponse> summarize(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        SummaryResponse response = medicalNlpService.summarize(request);
        return ApiResult.fromPayload(response, start);
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
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Keywords extracted",
                            content = @Content(schema = @Schema(implementation = KeywordResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ApiResult<KeywordResponse> keywords(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        KeywordResponse response = medicalNlpService.keywords(request);
        return ApiResult.fromPayload(response, start);
    }
}
