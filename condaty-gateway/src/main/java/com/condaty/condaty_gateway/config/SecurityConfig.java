package com.condaty.condaty_gateway.config;

import com.condaty.condaty_gateway.security.SecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final SecurityContextRepository securityContextRepository;

    public SecurityConfig(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Rutas públicas - Microservicio de Autenticación
                        .pathMatchers("/auth/**").permitAll()
                        
                        // Rutas públicas - Actuator y health checks
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/*/actuator/**").permitAll()  // Actuator de microservicios
                        
                        // Todas las demás rutas requieren autenticación JWT
                        .anyExchange().authenticated()
                )
                .securityContextRepository(securityContextRepository)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // Deshabilita el login básico
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)  // Deshabilita el formulario de login
                .build();
    }
}
