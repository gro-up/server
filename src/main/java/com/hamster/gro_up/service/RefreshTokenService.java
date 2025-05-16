package com.hamster.gro_up.service;

import com.hamster.gro_up.util.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String token, String email) {
        String key = getRefreshTokenKey(email);
        long expireMs = TokenType.REFRESH.getExpireMs();
        redisTemplate.opsForValue().set(key, token, expireMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String email) {
        String key = getRefreshTokenKey(email);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String email) {
        String key = getRefreshTokenKey(email);
        redisTemplate.delete(key);
    }

    public boolean existsByEmail(String email) {
        String key = getRefreshTokenKey(email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String getRefreshTokenKey(String email) {
        return "refreshToken:" + email;
    }
}
