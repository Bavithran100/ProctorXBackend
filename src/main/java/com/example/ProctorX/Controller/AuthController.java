package com.example.ProctorX.Controller;

import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://localhost:5174",
                 "http://localhost:5175",
                "https://proctor-x-frontend.vercel.app"
        },
        allowCredentials = "true"
)

public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    AuthenticationManager authenticationManager;
    @PostMapping("/Register")
    public ResponseEntity<?> register(@RequestBody AuthEntity authEntity) {
        // You may want to check if user exists already
        authService.setUser(authEntity);
        return ResponseEntity.ok("User registered");
    }
    @PostMapping("/Login")
    public ResponseEntity<?> login(@RequestBody AuthEntity request,
                                   HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create session
        httpRequest.getSession(true);

        AuthEntity user = authService.findByEmail(request.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                )
        );
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        System.out.println(authentication.getName());

        return ResponseEntity.ok(
                Map.of(
                        "email", authentication.getName(),
                        "role", authentication.getAuthorities().iterator().next().getAuthority() .replace("ROLE_", "")
                )
        );
    }








}
