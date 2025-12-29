package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.MedicalNlpService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nlp")
@CrossOrigin
public class MedicalNlpController {

    private final MedicalNlpService medicalNlpService;

    public MedicalNlpController(MedicalNlpService medicalNlpService) {
        this.medicalNlpService = medicalNlpService;
    }

    @PostMapping("/grammar")
    public ApiResponse<GrammarResponse> grammar(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        GrammarResponse response = medicalNlpService.checkGrammar(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/entities")
    public ApiResponse<EntityExtractionResponse> entities(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        EntityExtractionResponse response = medicalNlpService.extractEntities(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/summarize")
    public ApiResponse<SummaryResponse> summarize(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        SummaryResponse response = medicalNlpService.summarize(request);
        return ApiResponse.fromPayload(response, start);
    }

    @PostMapping("/keywords")
    public ApiResponse<KeywordResponse> keywords(@Valid @RequestBody ClinicalNoteRequest request) {
        long start = System.currentTimeMillis();
        KeywordResponse response = medicalNlpService.keywords(request);
        return ApiResponse.fromPayload(response, start);
    }
}
