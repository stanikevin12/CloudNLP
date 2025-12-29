package com.example.demo.controller;

import com.example.demo.entity.PatientReport;
import com.example.demo.service.PatientReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
@Tag(name = "Patient Reports", description = "CRUD operations for patient clinical notes")
public class PatientReportController {

    private final PatientReportService service;

    public PatientReportController(PatientReportService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    @Operation(
            summary = "Create a patient report",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PatientReport.class),
                            examples = @ExampleObject(value = "{\n  \"patientName\": \"John Doe\",\n  \"diagnosis\": \"Hypertension\",\n  \"notes\": \"Patient reports dizziness.\"\n}")
                    )
            ),
            responses = @ApiResponse(responseCode = "200", description = "Report created",
                    content = @Content(schema = @Schema(implementation = PatientReport.class)))
    )
    public PatientReport create(@RequestBody PatientReport report) {
        return service.create(report);
    }

    // READ ALL
    @GetMapping
    @Operation(summary = "List patient reports", responses = @ApiResponse(responseCode = "200", description = "Reports returned"))
    public List<PatientReport> getAll() {
        return service.getAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    @Operation(summary = "Get a patient report", responses = @ApiResponse(responseCode = "200", description = "Report returned"))
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
                            examples = @ExampleObject(value = "{\n  \"patientName\": \"John Doe\",\n  \"diagnosis\": \"Improved\",\n  \"notes\": \"Symptoms resolved.\"\n}")
                    )
            ),
            responses = @ApiResponse(responseCode = "200", description = "Report updated",
                    content = @Content(schema = @Schema(implementation = PatientReport.class)))
    )
    public PatientReport update(@PathVariable Long id,
                                @RequestBody PatientReport report) {
        return service.update(id, report);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient report", responses = @ApiResponse(responseCode = "200", description = "Report deleted"))
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
