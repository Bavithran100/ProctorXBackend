package com.example.ProctorX.Service;

import com.example.ProctorX.Entity.AuthEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    void setUser(AuthEntity authEntity);
    AuthEntity findByEmail(String email);
    public AuthEntity getCurrentUser(Authentication authentication);
}
