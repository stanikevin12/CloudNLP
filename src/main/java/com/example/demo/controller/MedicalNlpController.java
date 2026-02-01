package com.example.demo.controller;

import com.example.demo.dto.ApiResult;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.SummaryResponse;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/nlp")
@CrossOrigin
public class MedicalNlpController {

    private final GrammarService grammarService;
    private final SummarizationService summarizationService;
    private final KeywordExtractionService keywordService;
    private final EntityExtractionService entityService;

    public MedicalNlpController(
            GrammarService grammarService,
            SummarizationService summarizationService,
            KeywordExtractionService keywordService,
            EntityExtractionService entityService
    ) {
        this.grammarService = grammarService;
        this.summarizationService = summarizationService;
        this.keywordService = keywordService;
        this.entityService = entityService;
    }

    @PostMapping("/grammar")
    public ResponseEntity<ApiResult<GrammarResponse>> grammar(
           @Valid @RequestBody ClinicalNoteRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                ApiResult.success(200, servletRequest.getRequestURI(),
                        grammarService.checkGrammar(request))
        );
    }

    @PostMapping("/summarize")
    public ResponseEntity<ApiResult<SummaryResponse>> summarize(
           @Valid @RequestBody ClinicalNoteRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                ApiResult.success(200, servletRequest.getRequestURI(),
                        summarizationService.summarize(request))
        );
    }

    @PostMapping("/keywords")
    public ResponseEntity<ApiResult<KeywordResponse>> keywords(
           @Valid @RequestBody ClinicalNoteRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                ApiResult.success(200, servletRequest.getRequestURI(),
                        keywordService.extractKeywords(request))
        );
    }

    @PostMapping("/entities")
    public ResponseEntity<ApiResult<EntityExtractionResponse>> entities(
            @Valid @RequestBody ClinicalNoteRequest request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                ApiResult.success(200, servletRequest.getRequestURI(),
                        entityService.extractEntities(request))
        );
    }
}
