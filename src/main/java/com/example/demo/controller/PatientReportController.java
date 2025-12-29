package com.example.demo.controller;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.entity.PatientReport;
import com.example.demo.service.PatientReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                            content = @Content(schema = @Schema(implementation = PatientReport.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public PatientReport create(@Valid @RequestBody PatientReport report) {
        return service.create(report);
    }

    // READ ALL
    @GetMapping
    @Operation(summary = "List patient reports", responses = {
            @ApiResponse(responseCode = "200", description = "Reports returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<PatientReport> getAll() {
        return service.getAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    @Operation(summary = "Get a patient report", responses = {
            @ApiResponse(responseCode = "200", description = "Report returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PatientReport getById(@PathVariable Long id) {
        return service.getById(id);
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
                            content = @Content(schema = @Schema(implementation = PatientReport.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public PatientReport update(@PathVariable Long id,
                                @Valid @RequestBody PatientReport report) {
        return service.update(id, report);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient report", responses = {
            @ApiResponse(responseCode = "200", description = "Report deleted"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
