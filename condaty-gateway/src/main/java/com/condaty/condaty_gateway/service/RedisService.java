package com.condaty.condaty_gateway.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> saveToken(String key, String token, Duration expiration) {
        return redisTemplate.opsForValue().set(key, token, expiration);
    }

    public Mono<String> getToken(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> deleteToken(String key) {
        return redisTemplate.delete(key).map(count -> count > 0);
    }

    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(key);
    }
}
