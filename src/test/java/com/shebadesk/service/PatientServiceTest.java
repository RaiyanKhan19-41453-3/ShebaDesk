package com.shebadesk.service;

import com.shebadesk.dto.PatientRequestDTO;
import com.shebadesk.exception.EmailAlreadyExistsException;
import com.shebadesk.exception.PatientNotFoundException;
import com.shebadesk.model.Patient;
import com.shebadesk.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientRequestDTO request(String email) {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Test Patient");
        dto.setEmail(email);
        dto.setLocation("Dhaka");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setRegisteredDate(LocalDate.of(2026, 10, 1));
        return dto;
    }

    @Test
    void createPatient_duplicateEmail_throws409() {
        when(patientRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.createPatient(request("dup@test.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void getPatientById_missing_throws404() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(id))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void createPatient_ok_mapsAllFields() {
        when(patientRepository.existsByEmail("ok@test.com")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        var result = patientService.createPatient(request("ok@test.com"));

        assertThat(result.getEmail()).isEqualTo("ok@test.com");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getRegisteredDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(result.getId()).isNotNull();
    }
}
