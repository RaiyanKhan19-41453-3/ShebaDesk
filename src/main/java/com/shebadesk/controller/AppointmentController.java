package com.shebadesk.controller;

import com.shebadesk.dto.AppointmentRequestDTO;
import com.shebadesk.dto.AppointmentResponseDTO;
import com.shebadesk.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointments", description = "Appointment scheduling APIs")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "List appointments", description = "Paginated list with optional doctor/patient/date-range filters")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointments(
            @RequestParam(required = false) @Parameter(description = "Filter by doctor id") Long doctorId,
            @RequestParam(required = false) @Parameter(description = "Filter by patient id") UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Range start (requires 'to')") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Range end (requires 'from')") LocalDate to,
            @PageableDefault(size = 20, sort = "appointmentDate") @Parameter(description = "Paging and sorting") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointments(doctorId, patientId, from, to, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by id")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    @Operation(summary = "Book appointment", description = "Rejects past dates and double-bookings with 409")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO created = appointmentService.createAppointment(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel appointment")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }
}
