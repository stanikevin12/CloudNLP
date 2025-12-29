package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Schema(description = "Lightweight patient report used for demonstrations. Data is stored only in-memory.")
public class PatientReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Size(max = 255, message = "Patient name must be 255 characters or fewer")
    @Schema(description = "Patient name", example = "John Doe")
    @Column(length = 255)
    private String patientName;

    @NotBlank(message = "Report text is required")
    @Size(max = 5000, message = "Report text must be 5,000 characters or fewer")
    @Schema(description = "Clinical note content", example = "Patient reports dizziness and headache.")
    @Column(length = 5000)
    private String reportText;

    public PatientReport() {}

    public PatientReport(String patientName, String reportText) {
        this.patientName = patientName;
        this.reportText = reportText;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getReportText() { return reportText; }
    public void setReportText(String reportText) { this.reportText = reportText; }
}
