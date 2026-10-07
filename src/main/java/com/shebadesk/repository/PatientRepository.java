package com.shebadesk.repository;

import com.shebadesk.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Page<Patient> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Patient> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Patient> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}
