package com.condaty.auth_service.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.condaty.auth_service.dto.LoginRequest;
import com.condaty.auth_service.dto.LoginResponse;
import com.condaty.auth_service.dto.RegisterRequest;
import com.condaty.auth_service.dto.RegisterResponse;
import com.condaty.auth_service.entity.Person;
import com.condaty.auth_service.repository.PersonRepository;
import com.condaty.auth_service.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Procesa el login del usuario
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Intento de login para UUID: {}", loginRequest.getUuid());

        try {
            // Buscar la persona por UUID
            Person person = personRepository.findByUuid(loginRequest.getUuid())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con UUID: " + loginRequest.getUuid()));

            // Verificar si el usuario está activo
            if (!person.getIsActive()) {
                log.warn("Intento de login de usuario inactivo: {}", loginRequest.getUuid());
                throw new BadCredentialsException("Usuario inactivo");
            }

            // Verificar si tiene contraseña
            if (person.getPassword() == null || person.getPassword().isEmpty()) {
                log.warn("Usuario sin contraseña configurada: {}", loginRequest.getUuid());
                throw new BadCredentialsException("Usuario sin credenciales configuradas");
            }

            // Autenticar usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUuid(),
                            loginRequest.getPassword()
                    )
            );

            // Generar token JWT con roles
            java.util.List<String> roles = java.util.Arrays.asList("ROLE_USER");
            String token = jwtUtil.generateTokenWithRoles(person.getUuid(), roles);

            // Guardar token en Redis con la clave que el gateway espera: session:{uuid}:{token}
            String redisKey = "session:" + person.getUuid() + ":" + token;
            redisTemplate.opsForValue().set(
                    redisKey,
                    token,
                    jwtUtil.getExpirationTime(),
                    TimeUnit.MILLISECONDS
            );

            log.info("Login exitoso para UUID: {}", person.getUuid());

            // Construir respuesta
            return new LoginResponse(
                    token,
                    person.getUuid(),
                    person.getFirstName(),
                    person.getPaternalName(),
                    person.getEmail(),
                    jwtUtil.getExpirationTime()
            );

        } catch (BadCredentialsException e) {
            log.error("Credenciales inválidas para UUID: {}", loginRequest.getUuid());
            throw new BadCredentialsException("UUID o contraseña incorrectos");
        } catch (UsernameNotFoundException e) {
            log.error("Usuario no encontrado: {}", loginRequest.getUuid());
            throw e;
        } catch (Exception e) {
            log.error("Error durante el login: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el login", e);
        }
    }

    /**
     * Procesa el logout del usuario
     */
    public void logout(String uuid, String token) {
        log.info("Procesando logout para UUID: {}", uuid);

        try {
            // Eliminar el token de Redis usando la clave que el gateway espera
            String redisKey = "session:" + uuid + ":" + token;
            Boolean deleted = redisTemplate.delete(redisKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("Logout exitoso para UUID: {}", uuid);
            } else {
                log.warn("No se encontró token activo para UUID: {}", uuid);
            }

        } catch (Exception e) {
            log.error("Error durante el logout: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el logout", e);
        }
    }

    /**
     * Registra un nuevo usuario
     */
    public RegisterResponse register(RegisterRequest request) {
        log.info("Intento de registro para documento: {}", request.getUuid());

        try {
            // Verificar si ya existe
            if (personRepository.existsByUuid(request.getUuid())) {
                log.warn("Intento de registro con documento duplicado: {}", request.getUuid());
                throw new RuntimeException("Ya existe un usuario con ese documento");
            }

            if (request.getEmail() != null && personRepository.existsByEmail(request.getEmail())) {
                log.warn("Intento de registro con email duplicado: {}", request.getEmail());
                throw new RuntimeException("Ya existe un usuario con ese email");
            }

            // Encriptar contraseña solo si se proporciona
            String hashedPassword = null;
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                hashedPassword = passwordEncoder.encode(request.getPassword());
            }

            // Crear persona
            Person person = Person.builder()
                    .uuid(request.getUuid())
                    .firstName(request.getFirstName())
                    .secondName(request.getSecondName())
                    .paternalName(request.getPaternalName())
                    .motherName(request.getMotherName())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .birthday(request.getBirthday())
                    .password(hashedPassword)
                    .isActive(true)
                    .build();

            personRepository.save(person);

            log.info("Usuario registrado exitosamente: {}", person.getUuid());

            return RegisterResponse.builder()
                    .uuid(person.getUuid())
                    .firstName(person.getFirstName())
                    .paternalName(person.getPaternalName())
                    .email(person.getEmail())
                    .message("Usuario registrado exitosamente")
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error durante el registro: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el registro: " + e.getMessage());
        }
    }
}
