package com.mgt.hospital.repository;

import com.mgt.hospital.model.Doctor;
import com.mgt.hospital.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
