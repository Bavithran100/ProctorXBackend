package com.example.ProctorX.Service.Impl;

import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Repository.AuthRepository;
import com.example.ProctorX.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    AuthRepository authRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void setUser(AuthEntity authEntity) {
        // Encode the password before saving
        String raw = authEntity.getPassword();
        authEntity.setPassword(passwordEncoder.encode(raw));
        // default role if not set
        if (authEntity.getRole() == null) {
            authEntity.setRole(AuthEntity.Role.STUDENT);
        }
        authEntity.setEnabled(true);
        authRepository.save(authEntity);
    }
    @Override
    public AuthEntity findByEmail(String email) {
        return authRepository.findByEmail(email);
    }
    public AuthEntity getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName(); // comes from session

        return authRepository.findByEmail(email);
    }


}