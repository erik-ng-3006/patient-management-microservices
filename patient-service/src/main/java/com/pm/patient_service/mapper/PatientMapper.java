package com.pm.patient_service.mapper;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.model.Patient;

import java.time.LocalDate;

public class PatientMapper {
  public static PatientResponseDTO toDTO(Patient patient) {
    PatientResponseDTO patientResponseDTO = new PatientResponseDTO();
    patientResponseDTO.setId(patient.getId());
    patientResponseDTO.setName(patient.getName());
    patientResponseDTO.setEmail(patient.getEmail());
    patientResponseDTO.setAddress(patient.getAddress());
    patientResponseDTO.setDateOfBirth(patient.getDateOfBirth());
    return patientResponseDTO;
  }

  public static Patient toEntity(PatientRequestDTO patientRequestDTO) {
    Patient patient = new Patient();
    patient.setName(patientRequestDTO.getName());
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
    patient.setRegisteredDate(LocalDate.parse(patientRequestDTO.getRegisteredDate()));
    return patient;
  }
}
