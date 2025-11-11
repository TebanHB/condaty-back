package com.condaty.condaty_gateway.security;

import com.condaty.condaty_gateway.service.RedisService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public AuthenticationManager(JwtUtil jwtUtil, RedisService redisService) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        try {
            // 1. Validar JWT (firma y expiración)
            if (Boolean.FALSE.equals(jwtUtil.validateToken(authToken))) {
                return Mono.empty();
            }

            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            String username = claims.getSubject();
            
            // 2. Validar contra Redis (verificar si el token está en whitelist)
            return redisService.exists("session:" + username + ":" + authToken)
                    .flatMap(exists -> {
                        if (Boolean.FALSE.equals(exists)) {
                            // Token no está en Redis = sesión inválida o logout
                            return Mono.<Authentication>empty();
                        }

                        // Token válido y existe en Redis
                        @SuppressWarnings("unchecked")
                        List<String> roles = claims.get("roles", List.class);

                        List<SimpleGrantedAuthority> authorities = roles != null
                                ? roles.stream().map(SimpleGrantedAuthority::new).toList()
                                : Collections.emptyList();

                        return Mono.<Authentication>just(new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        ));
                    })
                    .onErrorResume(e -> Mono.empty());  // Si Redis falla, rechazar
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
