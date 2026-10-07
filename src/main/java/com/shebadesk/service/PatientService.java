package com.shebadesk.service;

import com.shebadesk.dto.PatientRequestDTO;
import com.shebadesk.dto.PatientResponseDTO;
import com.shebadesk.exception.EmailAlreadyExistsException;
import com.shebadesk.exception.PatientNotFoundException;
import com.shebadesk.mapper.PatientMapper;
import com.shebadesk.model.Patient;
import com.shebadesk.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getPatients(Pageable pageable, String name, String email){
        Page<Patient> page;
        boolean hasName = name != null && !name.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        if (hasName && hasEmail) {
            page = patientRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(name, email, pageable);
        } else if (hasName) {
            page = patientRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasEmail) {
            page = patientRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            page = patientRepository.findAll(pageable);
        }
        return page.map(PatientMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(UUID id){
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        return PatientMapper.toDTO(patient);
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with this email already exists: " + patientRequestDTO.getEmail());
        }
        Patient patient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO)
        );

        PatientResponseDTO patientResponseDTO = PatientMapper.toDTO(patient);
        return patientResponseDTO;
    }

    @Transactional
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)){
            throw new EmailAlreadyExistsException("A patient with this email already exists: " + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setLocation(patientRequestDTO.getLocation());
        patient.setDateOfBirth(patientRequestDTO.getDateOfBirth());

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    @Transactional
    public void deletePatient(UUID id){
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        patientRepository.delete(patient);
    }
}
