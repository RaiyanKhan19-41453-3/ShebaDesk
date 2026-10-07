package com.shebadesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DoctorRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name can not exceed 100 characters")
    private String name;

    @NotBlank(message = "Specialty is required")
    @Size(max = 100, message = "Specialty can not exceed 100 characters")
    private String specialty;

    @NotBlank(message = "Email is required")
    @Email(message = "Provide valid email")
    private String email;

    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
