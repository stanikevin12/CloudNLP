package com.example.demo.controller;

import com.example.demo.dto.ApiResult;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.service.NlpCloudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

@Controller
@Validated
@Tag(name = "Classification", description = "UI helper endpoint for zero-shot classification")
public class AnalysisController {

    private final NlpCloudService service;

    public AnalysisController(NlpCloudService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/analyze")
    @ResponseBody
    @Operation(
            summary = "Classify ad-hoc text",
            description = "Performs zero-shot classification for UI form submissions.",
            parameters = @Parameter(name = "text", description = "Text to classify", required = true, example = "SpaceX launched a new rocket"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Classification succeeded",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "502", description = "NLP provider unavailable",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)))
            }
    )
    public ResponseEntity<ApiResult<ClassificationResponse>> analyze(@RequestParam @NotBlank(message = "Text is required") String text,
                                                                     HttpServletRequest request) {
        ClassificationResponse response = service.classify(text);
        ApiResult<ClassificationResponse> body = ApiResult.success(200, request.getRequestURI(), response);
        return ResponseEntity.ok(body);
    }
}
