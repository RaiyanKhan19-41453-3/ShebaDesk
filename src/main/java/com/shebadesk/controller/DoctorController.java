package com.shebadesk.controller;

import com.shebadesk.dto.AppointmentResponseDTO;
import com.shebadesk.dto.DoctorRequestDTO;
import com.shebadesk.dto.DoctorResponseDTO;
import com.shebadesk.service.AppointmentService;
import com.shebadesk.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/doctors")
@Tag(name = "Doctors", description = "Doctor management APIs")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "List doctors", description = "Paginated list with optional name/specialty search")
    public ResponseEntity<Page<DoctorResponseDTO>> getDoctors(
            @PageableDefault(size = 20, sort = "name") @Parameter(description = "Paging and sorting") Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Filter by name (contains, case-insensitive)") String name,
            @RequestParam(required = false) @Parameter(description = "Filter by specialty (contains, case-insensitive)") String specialty) {
        return ResponseEntity.ok(doctorService.getDoctors(pageable, name, specialty));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by id")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping
    @Operation(summary = "Create doctor")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO dto) {
        DoctorResponseDTO created = doctorService.createDoctor(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update doctor")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete doctor")
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
    }

    @GetMapping("/{id}/appointments")
    @Operation(summary = "List appointments for a doctor")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsForDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(id));
    }
}
