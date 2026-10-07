package com.shebadesk.repository;

import com.shebadesk.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Page<Doctor> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Doctor> findBySpecialtyContainingIgnoreCase(String specialty, Pageable pageable);
    Page<Doctor> findByNameContainingIgnoreCaseAndSpecialtyContainingIgnoreCase(String name, String specialty, Pageable pageable);
}
