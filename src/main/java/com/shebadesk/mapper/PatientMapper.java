package com.shebadesk.mapper;

import com.shebadesk.dto.PatientRequestDTO;
import com.shebadesk.dto.PatientResponseDTO;
import com.shebadesk.model.Patient;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO patientDTO = new PatientResponseDTO();
        patientDTO.setId(patient.getId() != null ? patient.getId().toString() : null);
        patientDTO.setName(patient.getName());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setLocation(patient.getLocation());
        patientDTO.setDateOfBirth(patient.getDateOfBirth());
        patientDTO.setRegisteredDate(patient.getRegisteredDate());
        return patientDTO;
    }

    public static Patient toModel(PatientRequestDTO patientRequestDTO){
        Patient patient = new Patient();
        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setLocation(patientRequestDTO.getLocation());
        patient.setDateOfBirth(patientRequestDTO.getDateOfBirth());
        patient.setRegisteredDate(patientRequestDTO.getRegisteredDate());
        return patient;
    }
}
