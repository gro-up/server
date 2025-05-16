package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.auth.*;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.UserRepository;
import com.hamster.gro_up.util.JwtUtil;
import com.hamster.gro_up.util.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse signUp(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateUserException();
        }

        if (!emailVerificationService.isEmailVerified(signupRequest.getEmail())) {
            throw new EmailNotVerifiedException();
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        refreshTokenService.saveRefreshToken(refreshToken, savedUser.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    public TokenResponse signIn(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                UserNotFoundException::new);

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, user.getId(), user.getEmail(), user.getRole());

        refreshTokenService.saveRefreshToken(refreshToken, user.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    public void signOut(String refreshToken) {
        if (jwtUtil.isExpired(refreshToken)) {
            throw new ExpiredTokenException();
        }

        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!tokenType.equalsIgnoreCase(TokenType.REFRESH.name())) {
            throw new InvalidTokenException("Refresh Token 이 아닙니다.");
        }

        String email = jwtUtil.getEmail(refreshToken);
        if (!refreshTokenService.existsByEmail(email)) {
            throw new InvalidTokenException("이미 로그아웃된 사용자입니다.");
        }

        refreshTokenService.deleteRefreshToken(email);
    }

    public TokenResponse reissueAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException();
        }

        if (jwtUtil.isExpired(refreshToken)) {
            throw new ExpiredTokenException();
        }

        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!tokenType.equalsIgnoreCase(TokenType.REFRESH.name())) {
            throw new TokenTypeMismatchException();
        }

        AuthUser authUser = jwtUtil.getAuthUserFromToken(refreshToken);
        String email = authUser.getEmail();

        String storedRefreshToken = refreshTokenService.getRefreshToken(authUser.getEmail());
        if (storedRefreshToken == null) {
            throw new TokenNotFoundException();
        }

        log.info("storedRefreshToken: {}", storedRefreshToken);
        log.info("refreshToken from request: {}", refreshToken);
        log.info("equals: {}", storedRefreshToken.equals(refreshToken));

        if (!storedRefreshToken.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh Token 이 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createToken(TokenType.ACCESS, authUser.getId(), email, authUser.getRole());
        String newRefreshToken = jwtUtil.createToken(TokenType.REFRESH, authUser.getId(), email, authUser.getRole());

        // Refresh Token Rotation (RTR)
        refreshTokenService.deleteRefreshToken(email);
        refreshTokenService.saveRefreshToken(newRefreshToken, email);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
