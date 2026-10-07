package com.shebadesk.mapper;

import com.shebadesk.dto.DoctorRequestDTO;
import com.shebadesk.dto.DoctorResponseDTO;
import com.shebadesk.model.Doctor;

public class DoctorMapper {
    public static DoctorResponseDTO toDTO(Doctor doctor) {
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        return dto;
    }

    public static Doctor toModel(DoctorRequestDTO dto) {
        Doctor doctor = new Doctor();
        doctor.setName(dto.getName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        return doctor;
    }
}
