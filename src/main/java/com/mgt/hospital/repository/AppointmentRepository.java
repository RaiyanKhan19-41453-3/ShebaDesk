package com.mgt.hospital.repository;

import com.mgt.hospital.model.Appointment;
import com.mgt.hospital.model.Doctor;
import com.mgt.hospital.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository  extends JpaRepository<Appointment, Long> {
    // find appointments by doctor id
    List<Appointment> findByDoctorId(Long doctorId);

    // find appointments by patient id
    List<Appointment> findByPatientId(Long patientId);

    // find appointment by doctor and patient
    Optional<Appointment> findByDoctorIdAndPatientId(Long doctorId, Long patientId);

    // find appointments on a specific date
    List<Appointment> findByAppointmentDate(LocalDate date);

    // find appointments with notes containing some text
    List<Appointment> findByNotesContaining(String keyword);



//    // Get all patients for a doctor
//    @Query("SELECT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
//    List<Patient> findPatientsByDoctorIdexample(@Param("doctorId") Long doctorId);
//
//    // Get all doctors for a patient
//    @Query("SELECT a.doctor FROM Appointment a WHERE a.patient.id = :patientId")
//    List<Doctor> findDoctorsByPatientIdexample(@Param("patientId") Long patientId);
}
