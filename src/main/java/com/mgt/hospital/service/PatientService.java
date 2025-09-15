package com.mgt.hospital.service;

import com.mgt.hospital.dto.PatientRequestDTO;
import com.mgt.hospital.dto.PatientResponseDTO;
import com.mgt.hospital.exception.EmailAlreadyExistsException;
import com.mgt.hospital.mapper.PatientMapper;
import com.mgt.hospital.model.Patient;
import com.mgt.hospital.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        List<PatientResponseDTO> patientResponseDTOs = patients.stream().map(patient -> PatientMapper.toDTO(patient)).toList();

        return  patientResponseDTOs;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email already exist " + patientRequestDTO.getEmail());
        }
        Patient patient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO)
        );

        PatientResponseDTO patientResponseDTO = PatientMapper.toDTO(patient);
        return patientResponseDTO;
    }
}
