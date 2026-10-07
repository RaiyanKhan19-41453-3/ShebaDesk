package com.shebadesk.repository;

import com.shebadesk.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @EntityGraph(attributePaths = {"doctor", "patient"})
    List<Appointment> findByDoctorId(Long doctorId);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Optional<Appointment> findById(Long id);

    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByDoctorIdAndPatientId(Long doctorId, UUID patientId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByPatientIdAndAppointmentDateBetween(UUID patientId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByDoctorIdAndPatientIdAndAppointmentDateBetween(Long doctorId, UUID patientId, LocalDate from, LocalDate to, Pageable pageable);
}
