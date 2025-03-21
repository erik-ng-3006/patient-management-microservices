package com.pm.patient_service.controller;

import java.util.List;
import java.util.UUID;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.validators.CreatePatientValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.service.PatientService;


@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {
  private final PatientService patientService;

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @GetMapping
  @Operation(summary = "Get all patients")
  public ResponseEntity<List<PatientResponseDTO>> getPatients() {
    List<PatientResponseDTO> patients = patientService.getPatients();
    return ResponseEntity.ok().body(patients);
  }

  @PostMapping
  @Operation(summary = "Create a new patient")
  public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
    PatientResponseDTO createdPatient = patientService.createPatient(patientRequestDTO);
    return ResponseEntity.ok().body(createdPatient);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a patient")
  public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated(Default.class) @RequestBody PatientRequestDTO patientRequestDTO) {
    PatientResponseDTO updatedPatient = patientService.updatePatient(id, patientRequestDTO);
    return ResponseEntity.ok().body(updatedPatient);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a patient")
  public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
  }
}
