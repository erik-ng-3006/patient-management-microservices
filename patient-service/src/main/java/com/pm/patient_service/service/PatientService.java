package com.pm.patient_service.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.exception.EmailAlreadyExistException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.model.Patient;
import org.springframework.stereotype.Service;

import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.repository.PatientRepository;

@Service
public class PatientService {
  private final PatientRepository patientRepository;

  public PatientService(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  public List<PatientResponseDTO> getPatients() {
    return patientRepository.findAll().stream().map(PatientMapper::toDTO).toList();
  }

  public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
    //check if email exists
    if (patientRepository.existsPatientByEmail(patientRequestDTO.getEmail())) {
      throw new EmailAlreadyExistException("A patient with email " + patientRequestDTO.getEmail() + " already exists");
    }
    return PatientMapper.toDTO(patientRepository.save(PatientMapper.toEntity(patientRequestDTO)));
  }

  public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
    Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

    //check if email exists and is not the current email of the patient
    if (patientRepository.existsPatientByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
      throw new EmailAlreadyExistException("A patient with email " + patientRequestDTO.getEmail() + " already exists");
    }

    patient.setName(patientRequestDTO.getName());
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
    return PatientMapper.toDTO(patientRepository.save(patient));
  }

  public void deletePatient(UUID id) {
    patientRepository.deleteById(id);
  }
}
