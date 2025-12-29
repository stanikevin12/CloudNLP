package com.example.demo.controller;

import com.example.demo.config.MedicalDisclaimerFilter;
import com.example.demo.dto.ApiResult;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.SummaryResponse;
import com.example.demo.service.MedicalNlpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/nlp")
@CrossOrigin
@Tag(name = "Medical NLP", description = "Clinical NLP utilities routed Controller → Service → Mapper → upstream NLP engine")
public class MedicalNlpController {

    private static final Logger log = LoggerFactory.getLogger(MedicalNlpController.class);

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
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER))
            }
    )
    public ResponseEntity<ApiResult<GrammarResponse>> grammar(@Valid @RequestBody ClinicalNoteRequest request, HttpServletRequest servletRequest) {
        log.info("Received grammar check request at /api/nlp/grammar");
        GrammarResponse response = medicalNlpService.checkGrammar(request);
        log.info("Completed grammar check for /api/nlp/grammar");
        ApiResult<GrammarResponse> body = ApiResult.success(200, servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
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
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER))
            }
    )
    public ResponseEntity<ApiResult<EntityExtractionResponse>> entities(@Valid @RequestBody ClinicalNoteRequest request, HttpServletRequest servletRequest) {
        log.info("Received entity extraction request at /api/nlp/entities");
        EntityExtractionResponse response = medicalNlpService.extractEntities(request);
        log.info("Completed entity extraction for /api/nlp/entities");
        ApiResult<EntityExtractionResponse> body = ApiResult.success(200, servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
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
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER))
            }
    )
    public ResponseEntity<ApiResult<SummaryResponse>> summarize(@Valid @RequestBody ClinicalNoteRequest request, HttpServletRequest servletRequest) {
        log.info("Received summarization request at /api/nlp/summarize");
        SummaryResponse response = medicalNlpService.summarize(request);
        log.info("Completed summarization for /api/nlp/summarize");
        ApiResult<SummaryResponse> body = ApiResult.success(200, servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
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
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER)),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)),
                            headers = @Header(name = MedicalDisclaimerFilter.DISCLAIMER_HEADER, description = ApiResult.MEDICAL_DISCLAIMER))
            }
    )
    public ResponseEntity<ApiResult<KeywordResponse>> keywords(@Valid @RequestBody ClinicalNoteRequest request, HttpServletRequest servletRequest) {
        log.info("Received keyword extraction request at /api/nlp/keywords");
        KeywordResponse response = medicalNlpService.keywords(request);
        log.info("Completed keyword extraction for /api/nlp/keywords");
        ApiResult<KeywordResponse> body = ApiResult.success(200, servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
    }
}
