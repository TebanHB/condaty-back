package com.condaty.auth_service.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.condaty.auth_service.entity.Person;
import com.condaty.auth_service.repository.PersonRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        log.debug("Cargando usuario por UUID: {}", uuid);

        Person person = personRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado con UUID: {}", uuid);
                    return new UsernameNotFoundException("Usuario no encontrado con UUID: " + uuid);
                });

        // Verificar que el usuario esté activo
        if (!person.getIsActive()) {
            log.warn("Usuario inactivo intentando autenticarse: {}", uuid);
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        // Verificar que tenga contraseña
        if (person.getPassword() == null || person.getPassword().isEmpty()) {
            log.warn("Usuario sin contraseña: {}", uuid);
            throw new UsernameNotFoundException("Usuario sin credenciales");
        }

        log.debug("Usuario cargado exitosamente: {}", uuid);

        // Retornar UserDetails con el UUID como username
        return User.builder()
                .username(person.getUuid())
                .password(person.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!person.getIsActive())
                .build();
    }
}
