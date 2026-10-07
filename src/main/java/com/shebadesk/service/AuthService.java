package com.shebadesk.service;

import com.shebadesk.dto.LoginRequestDTO;
import com.shebadesk.dto.LoginResponseDTO;
import com.shebadesk.dto.RegisterRequestDTO;
import com.shebadesk.exception.EmailAlreadyExistsException;
import com.shebadesk.exception.InvalidCredentialsException;
import com.shebadesk.mapper.UserMapper;
import com.shebadesk.model.AppUser;
import com.shebadesk.repository.UserRepository;
import com.shebadesk.security.JwtService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        AppUser user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponseDTO(token, jwtService.getExpirationSeconds());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void register(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new EmailAlreadyExistsException("Username already taken: " + dto.getUsername());
        }
        userRepository.save(UserMapper.toModel(dto, passwordEncoder.encode(dto.getPassword())));
    }
}
