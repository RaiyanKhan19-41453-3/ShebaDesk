package com.shebadesk.mapper;

import com.shebadesk.dto.RegisterRequestDTO;
import com.shebadesk.model.AppUser;

public class UserMapper {
    public static AppUser toModel(RegisterRequestDTO dto, String passwordHash) {
        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordHash);
        user.setRole(dto.getRole());
        return user;
    }
}
