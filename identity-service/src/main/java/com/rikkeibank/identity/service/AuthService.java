package com.rikkeibank.identity.service;

import com.rikkeibank.common.exception.BusinessException;
import com.rikkeibank.common.exception.ResourceNotFoundException;
import com.rikkeibank.identity.config.JwtService;
import com.rikkeibank.identity.dto.*;
import com.rikkeibank.identity.entity.User;
import com.rikkeibank.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId().toString(), List.of(user.getRole().name()));
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        // Store refresh token in Redis
        String key = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS);

        return new AuthResponse(accessToken, refreshToken, user.getId().toString(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        if (!user.getEnabled()) {
            throw new BusinessException("Account is disabled");
        }

        String accessToken = jwtService.generateAccessToken(user.getId().toString(), List.of(user.getRole().name()));
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        // Store refresh token in Redis
        String key = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(key, refreshToken, 30, TimeUnit.DAYS);

        return new AuthResponse(accessToken, refreshToken, user.getId().toString(), user.getRole().name());
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verify refresh token in Redis
        String key = "refresh_token:" + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        if (!refreshToken.equals(storedToken)) {
            throw new BusinessException("Invalid refresh token");
        }

        String accessToken = jwtService.generateAccessToken(userId, List.of(user.getRole().name()));

        return new AuthResponse(accessToken, refreshToken, userId, user.getRole().name());
    }

    public void logout(String userId, String accessToken) {
        // Blacklist access token
        String blacklistKey = "blacklist:token:" + accessToken;
        redisTemplate.opsForValue().set(blacklistKey, "revoked", 15, TimeUnit.MINUTES);

        // Remove refresh token
        String refreshKey = "refresh_token:" + userId;
        redisTemplate.delete(refreshKey);
    }

    public void revokeToken(String userId, String accessToken) {
        // Admin can revoke any user's token
        String blacklistKey = "blacklist:token:" + accessToken;
        redisTemplate.opsForValue().set(blacklistKey, "revoked", 15, TimeUnit.MINUTES);

        String refreshKey = "refresh_token:" + userId;
        redisTemplate.delete(refreshKey);
    }
}
