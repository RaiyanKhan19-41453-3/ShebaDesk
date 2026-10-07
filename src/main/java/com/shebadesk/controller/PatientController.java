package com.shebadesk.controller;

import com.shebadesk.dto.PatientRequestDTO;
import com.shebadesk.dto.PatientResponseDTO;
import com.shebadesk.dto.validators.CreatePatientValidationGroup;
import com.shebadesk.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patients", description = "Patient management APIs")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "List patients", description = "Paginated list with optional name/email search")
    public ResponseEntity<Page<PatientResponseDTO>> getPatients(
            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Paging and sorting") Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Filter by name (contains, case-insensitive)") String name,
            @RequestParam(required = false) @Parameter(description = "Filter by email (contains, case-insensitive)") String email){
        return ResponseEntity.ok(patientService.getPatients(pageable, name, email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by id")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable UUID id){
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PostMapping
    @Operation(summary = "Create patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO){
        PatientResponseDTO patient = patientService.createPatient(patientRequestDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(patient.getId())
                .toUri();
        return ResponseEntity.created(location).body(patient);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO){
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete patient")
    public void deletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
    }
}
