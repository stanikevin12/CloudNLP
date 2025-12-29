package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Clinical free text payload used for NLP tasks")
public class ClinicalNoteRequest {

    @NotBlank(message = "Clinical note is required")
    @Size(max = 10000, message = "Clinical note must be 10,000 characters or fewer")
    @Schema(description = "Primary clinical note or report", example = "Patient presents with chest pain and shortness of breath.")
    private String note;

    @Size(max = 2000, message = "Context must be 2,000 characters or fewer")
    @Schema(description = "Optional additional context such as vitals or history", example = "BP 130/90, history of hypertension")
    private String patientContext;

    public ClinicalNoteRequest() {
    }

    public ClinicalNoteRequest(String note, String patientContext) {
        this.note = note;
        this.patientContext = patientContext;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPatientContext() {
        return patientContext;
    }

    public void setPatientContext(String patientContext) {
        this.patientContext = patientContext;
    }
}
