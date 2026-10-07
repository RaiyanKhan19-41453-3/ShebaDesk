package com.shebadesk.service;

import com.shebadesk.dto.AppointmentRequestDTO;
import com.shebadesk.dto.AppointmentResponseDTO;
import com.shebadesk.exception.AppointmentConflictException;
import com.shebadesk.exception.AppointmentNotFoundException;
import com.shebadesk.exception.DoctorNotFoundException;
import com.shebadesk.exception.PatientNotFoundException;
import com.shebadesk.mapper.AppointmentMapper;
import com.shebadesk.model.Appointment;
import com.shebadesk.model.Doctor;
import com.shebadesk.model.Patient;
import com.shebadesk.repository.AppointmentRepository;
import com.shebadesk.repository.DoctorRepository;
import com.shebadesk.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointments(Long doctorId, UUID patientId, LocalDate from, LocalDate to, Pageable pageable) {
        if ((from != null && to == null) || (from == null && to != null)) {
            throw new IllegalArgumentException("Both 'from' and 'to' query params must be provided together");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
        Page<Appointment> page;
        boolean hasRange = from != null && to != null;
        if (doctorId != null && patientId != null && hasRange) {
            page = appointmentRepository.findByDoctorIdAndPatientIdAndAppointmentDateBetween(doctorId, patientId, from, to, pageable);
        } else if (doctorId != null && patientId != null) {
            page = appointmentRepository.findByDoctorIdAndPatientId(doctorId, patientId, pageable);
        } else if (doctorId != null && hasRange) {
            page = appointmentRepository.findByDoctorIdAndAppointmentDateBetween(doctorId, from, to, pageable);
        } else if (patientId != null && hasRange) {
            page = appointmentRepository.findByPatientIdAndAppointmentDateBetween(patientId, from, to, pageable);
        } else if (doctorId != null) {
            page = appointmentRepository.findByDoctorId(doctorId, pageable);
        } else if (patientId != null) {
            page = appointmentRepository.findByPatientId(patientId, pageable);
        } else {
            page = appointmentRepository.findAll(pageable);
        }
        return page.map(AppointmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        return AppointmentMapper.toDTO(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsForDoctor(Long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));
        return appointmentRepository.findByDoctorId(doctorId).stream().map(AppointmentMapper::toDTO).toList();
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        if (dto.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor id is required");
        }
        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("Patient id is required");
        }
        if (dto.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }
        if (dto.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new AppointmentConflictException("Appointment date must be today or in the future");
        }
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + dto.getDoctorId()));
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + dto.getPatientId()));

        if (appointmentRepository.existsByDoctorIdAndAppointmentDate(dto.getDoctorId(), dto.getAppointmentDate())) {
            throw new AppointmentConflictException("Doctor already booked on " + dto.getAppointmentDate());
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setNotes(dto.getNotes());

        return AppointmentMapper.toDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        appointmentRepository.delete(appointment);
    }
}
