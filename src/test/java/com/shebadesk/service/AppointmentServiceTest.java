package com.shebadesk.service;

import com.shebadesk.dto.AppointmentRequestDTO;
import com.shebadesk.exception.AppointmentConflictException;
import com.shebadesk.model.Doctor;
import com.shebadesk.model.Patient;
import com.shebadesk.repository.AppointmentRepository;
import com.shebadesk.repository.DoctorRepository;
import com.shebadesk.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentRequestDTO request(Long doctorId, UUID patientId, LocalDate date) {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setDoctorId(doctorId);
        dto.setPatientId(patientId);
        dto.setAppointmentDate(date);
        return dto;
    }

    @Test
    void createAppointment_nullIds_throws400WithoutTouchingDb() {
        UUID pid = UUID.randomUUID();
        LocalDate future = LocalDate.now().plusDays(5);

        assertThatThrownBy(() -> appointmentService.createAppointment(request(null, pid, future)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> appointmentService.createAppointment(request(1L, null, future)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> appointmentService.createAppointment(request(1L, pid, null)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(doctorRepository, never()).findById(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_doubleBooking_throws409() {
        UUID pid = UUID.randomUUID();
        LocalDate future = LocalDate.now().plusDays(5);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));
        Patient patient = new Patient();
        patient.setId(pid);
        when(patientRepository.findById(pid)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, future)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request(1L, pid, future)))
                .isInstanceOf(AppointmentConflictException.class);
        verify(appointmentRepository, never()).save(any());
    }
}
