package com.shebadesk.service;

import com.shebadesk.dto.DoctorRequestDTO;
import com.shebadesk.dto.DoctorResponseDTO;
import com.shebadesk.exception.DoctorNotFoundException;
import com.shebadesk.exception.EmailAlreadyExistsException;
import com.shebadesk.mapper.DoctorMapper;
import com.shebadesk.model.Doctor;
import com.shebadesk.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctors(Pageable pageable, String name, String specialty) {
        Page<Doctor> page;
        boolean hasName = name != null && !name.isBlank();
        boolean hasSpecialty = specialty != null && !specialty.isBlank();
        if (hasName && hasSpecialty) {
            page = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(name, specialty, pageable);
        } else if (hasName) {
            page = doctorRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasSpecialty) {
            page = doctorRepository.findBySpecialtyContainingIgnoreCase(specialty, pageable);
        } else {
            page = doctorRepository.findAll(pageable);
        }
        return page.map(DoctorMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
        return DoctorMapper.toDTO(doctor);
    }

    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        if (doctorRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("A doctor with this email already exists: " + dto.getEmail());
        }
        Doctor saved = doctorRepository.save(DoctorMapper.toModel(dto));
        return DoctorMapper.toDTO(saved);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
        if (doctorRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A doctor with this email already exists: " + dto.getEmail());
        }
        doctor.setName(dto.getName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        return DoctorMapper.toDTO(doctorRepository.save(doctor));
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
        doctorRepository.delete(doctor);
    }
}
