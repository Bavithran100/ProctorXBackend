package com.example.ProctorX.Config;

import com.example.ProctorX.Entity.AuthEntity;
import com.example.ProctorX.Repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthRepository authRepository;

    // 1) CORS configuration so React dev server can call backend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:5174","https://proctor-x-frontend.vercel.app")); // React dev origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // allow cookies / session if you plan to use them
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("X-EXAM-WARNING"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 2) Password encoder bean (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3) UserDetailsService that loads user by email (used by AuthenticationManager)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AuthEntity user = authRepository.findByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + username);
            }
            // Build a UserDetails object that Spring Security can use
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword()) // encoded password from DB
                    .roles(user.getRole() == null ? "STUDENT" : user.getRole().name())
                    .build();
        };
    }

    // 4) AuthenticationManager bean (used to authenticate a username/password pair)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5) Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // use the CORS bean
                .csrf(csrf -> csrf.disable()) // for a simple REST setup; enable CSRF if you use cookie-protected forms in prod
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // ⭐ THIS IS THE MISSING PIECE
                .securityContext(securityContext ->
                        securityContext.requireExplicitSave(false)
                )
                .authorizeHttpRequests(authz -> authz
                        // public endpoints
                        .requestMatchers("/", "/api/Register", "/api/Login", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                        // all other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // We will handle login via a REST controller using AuthenticationManager,
                // so we disable formLogin to avoid collision with controller endpoints.
                // disable HTTP Basic by calling the configurer's disable() inside a lambda
                .httpBasic(httpBasic -> httpBasic.disable())
                // disable the default form login filter
                .formLogin(form -> form.disable());

        return http.build();
    }
}
