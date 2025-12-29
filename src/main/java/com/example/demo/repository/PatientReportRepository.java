package com.example.demo.repository;

import com.example.demo.entity.PatientReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientReportRepository extends JpaRepository<PatientReport, Long> {
}
