package com.shebadesk.controller;

import com.shebadesk.dto.LoginRequestDTO;
import com.shebadesk.dto.LoginResponseDTO;
import com.shebadesk.dto.RegisterRequestDTO;
import com.shebadesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login and user registration APIs")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Returns a Bearer JWT for the ADMIN or RECEPTIONIST user")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register user (ADMIN only)")
    public void register(@Valid @RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
    }
}
