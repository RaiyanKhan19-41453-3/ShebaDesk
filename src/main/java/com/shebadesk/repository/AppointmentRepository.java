package com.shebadesk.repository;

import com.shebadesk.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndPatientId(Long doctorId, UUID patientId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Appointment> findByPatientIdAndAppointmentDateBetween(UUID patientId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Appointment> findByDoctorIdAndPatientIdAndAppointmentDateBetween(Long doctorId, UUID patientId, LocalDate from, LocalDate to, Pageable pageable);
}
