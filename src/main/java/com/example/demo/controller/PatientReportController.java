package com.example.demo.controller;

import com.example.demo.dto.ApiResult;
import com.example.demo.entity.PatientReport;
import com.example.demo.service.PatientReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
@Tag(name = "Patient Reports", description = "Demo-only CRUD endpoints backed by in-memory storage")
public class PatientReportController {

    private final PatientReportService service;

    public PatientReportController(PatientReportService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    @Operation(
            summary = "Create a patient report",
            description = "Stores an in-memory representation of a clinical note for demonstration only.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PatientReport.class),
                            examples = @ExampleObject(value = "{\n  \"patientName\": \"John Doe\",\n  \"reportText\": \"Patient reports dizziness.\"\n}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report created",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)))
            }
    )
    public ResponseEntity<ApiResult<PatientReport>> create(@Valid @RequestBody PatientReport report, HttpServletRequest request) {
        PatientReport created = service.create(report);
        ApiResult<PatientReport> body = ApiResult.success(200, request.getRequestURI(), created);
        return ResponseEntity.ok(body);
    }

    // READ ALL
    @GetMapping
    @Operation(summary = "List patient reports", responses = {
            @ApiResponse(responseCode = "200", description = "Reports returned",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiResult.class)))
    })
    public ResponseEntity<ApiResult<List<PatientReport>>> getAll(HttpServletRequest request) {
        List<PatientReport> results = service.getAll();
        ApiResult<List<PatientReport>> body = ApiResult.success(200, request.getRequestURI(), results);
        return ResponseEntity.ok(body);
    }

    // READ ONE
    @GetMapping("/{id}")
    @Operation(summary = "Get a patient report", responses = {
            @ApiResponse(responseCode = "200", description = "Report returned",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiResult.class)))
    })
    public ResponseEntity<ApiResult<PatientReport>> getById(@PathVariable Long id, HttpServletRequest request) {
        PatientReport report = service.getById(id);
        ApiResult<PatientReport> body = ApiResult.success(200, request.getRequestURI(), report);
        return ResponseEntity.ok(body);
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a patient report",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = PatientReport.class),
                            examples = @ExampleObject(value = "{\n  \"patientName\": \"John Doe\",\n  \"reportText\": \"Symptoms resolved.\"\n}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report updated",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ApiResult.class)))
            }
    )
    public ResponseEntity<ApiResult<PatientReport>> update(@PathVariable Long id,
                                @Valid @RequestBody PatientReport report,
                                HttpServletRequest request) {
        PatientReport updated = service.update(id, report);
        ApiResult<PatientReport> body = ApiResult.success(200, request.getRequestURI(), updated);
        return ResponseEntity.ok(body);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient report", responses = {
            @ApiResponse(responseCode = "200", description = "Report deleted",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiResult.class)))
    })
    public ResponseEntity<ApiResult<Void>> delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(id);
        ApiResult<Void> body = ApiResult.success(200, request.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }
}
