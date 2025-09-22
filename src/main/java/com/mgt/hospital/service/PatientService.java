package com.mgt.hospital.service;

import com.mgt.hospital.dto.PatientRequestDTO;
import com.mgt.hospital.dto.PatientResponseDTO;
import com.mgt.hospital.exception.EmailAlreadyExistsException;
import com.mgt.hospital.exception.PatientNotFoundException;
import com.mgt.hospital.mapper.PatientMapper;
import com.mgt.hospital.model.Patient;
import com.mgt.hospital.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient Not found with id : " + id));

        if(patientRepository.existsByEmailNotId(patientRequestDTO.getEmail(), id)){
            throw new EmailAlreadyExistsException("A patient with this email already exist " + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName().toString());
        patient.setEmail(patientRequestDTO.getEmail().toString());
        patient.setLocation(patientRequestDTO.getLocation().toString());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
