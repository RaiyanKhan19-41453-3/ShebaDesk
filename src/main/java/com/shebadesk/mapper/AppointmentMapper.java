package com.shebadesk.mapper;

import com.shebadesk.dto.AppointmentResponseDTO;
import com.shebadesk.model.Appointment;

public class AppointmentMapper {
    public static AppointmentResponseDTO toDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        if (appointment.getDoctor() != null) {
            dto.setDoctorId(appointment.getDoctor().getId());
            dto.setDoctorName(appointment.getDoctor().getName());
        }
        if (appointment.getPatient() != null) {
            dto.setPatientId(appointment.getPatient().getId());
            dto.setPatientName(appointment.getPatient().getName());
        }
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
}
