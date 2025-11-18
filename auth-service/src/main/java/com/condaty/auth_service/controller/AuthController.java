package com.condaty.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.condaty.auth_service.dto.LoginRequest;
import com.condaty.auth_service.dto.LoginResponse;
import com.condaty.auth_service.dto.MessageResponse;
import com.condaty.auth_service.dto.RegisterRequest;
import com.condaty.auth_service.dto.RegisterResponse;
import com.condaty.auth_service.service.AuthService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint para login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Solicitud de login recibida para UUID: {}", loginRequest.getUuid());

        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            log.error("Usuario no encontrado: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.builder()
                            .message("Usuario no encontrado")
                            .success(false)
                            .build());

        } catch (BadCredentialsException e) {
            log.error("Credenciales inválidas: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.builder()
                            .message("UUID o contraseña incorrectos")
                            .success(false)
                            .build());

        } catch (Exception e) {
            log.error("Error interno durante el login: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.builder()
                            .message("Error al procesar la solicitud de login")
                            .success(false)
                            .build());
        }
    }

    /**
     * Endpoint para logout
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Solicitud de logout recibida para usuario: {}", userDetails.getUsername());

        try {
            String uuid = userDetails.getUsername();
            
            // Extraer el token del header Authorization
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            
            if (token == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(MessageResponse.builder()
                                .message("Token no proporcionado")
                                .success(false)
                                .build());
            }
            
            authService.logout(uuid, token);

            return ResponseEntity.ok(MessageResponse.builder()
                    .message("Logout exitoso")
                    .success(true)
                    .build());

        } catch (Exception e) {
            log.error("Error durante el logout: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.builder()
                            .message("Error al procesar el logout")
                            .success(false)
                            .build());
        }
    }

    /**
     * Endpoint para verificar el estado de autenticación
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.builder()
                            .message("No autenticado")
                            .success(false)
                            .build());
        }

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Usuario autenticado: " + userDetails.getUsername())
                .success(true)
                .build());
    }

    /**
     * Health check del servicio de autenticación
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Auth service is running")
                .success(true)
                .build());
    }

    /**
     * Endpoint para registrar un nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Solicitud de registro para documento: {}", registerRequest.getUuid());

        try {
            RegisterResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(MessageResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build());
        }
    }
}
