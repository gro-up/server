package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.PasswordCheckRequest;
import com.hamster.gro_up.dto.request.PasswordUpdateRequest;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.entity.UserType;
import com.hamster.gro_up.exception.auth.*;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.UserRepository;
import com.hamster.gro_up.util.JwtUtil;
import com.hamster.gro_up.util.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

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
                .userType(UserType.LOCAL)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, UserType.LOCAL, savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, UserType.LOCAL, savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        refreshTokenService.saveRefreshToken(refreshToken, savedUser.getEmail());

        return TokenResponse.of(accessToken, refreshToken);
    }

    public TokenResponse signIn(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                UserNotFoundException::new);

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, UserType.LOCAL, user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, UserType.LOCAL, user.getId(), user.getEmail(), user.getRole());

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

        if (!storedRefreshToken.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh Token 이 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createToken(TokenType.ACCESS, authUser.getUserType(), authUser.getId(), email, authUser.getRole());
        String newRefreshToken = jwtUtil.createToken(TokenType.REFRESH, authUser.getUserType(), authUser.getId(), email, authUser.getRole());

        // Refresh Token Rotation (RTR)
        refreshTokenService.deleteRefreshToken(email);
        refreshTokenService.saveRefreshToken(newRefreshToken, email);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void deleteAccount(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        refreshTokenService.deleteRefreshToken(authUser.getEmail());

        userRepository.delete(user);
    }

    public void checkPassword(AuthUser authUser, PasswordCheckRequest passwordCheckRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(passwordCheckRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(token, user.getId().toString(), 10, TimeUnit.MINUTES);

        //TODO: front-end url 로 변경해야함.
        String resetUrl = "https://gro-up.shop/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("비밀번호 재설정 안내");
        message.setText("아래 링크를 클릭해 비밀번호를 재설정하세요:\n" + resetUrl);
        mailSender.send(message);
    }

    @Transactional
    public void resetPasswordWithToken(String token, PasswordUpdateRequest passwordUpdateRequest) {
        String userId = redisTemplate.opsForValue().get(token);

        if(userId == null) {
            throw new InvalidTokenException();
        }

        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(UserNotFoundException::new);

        String encodedNewPassword = passwordEncoder.encode(passwordUpdateRequest.getPassword());

        user.updatePassword(encodedNewPassword);

        redisTemplate.delete(token);

        refreshTokenService.deleteRefreshToken(user.getEmail());
    }

    @Transactional
    public void resetPasswordWithEmail(String email, PasswordUpdateRequest passwordUpdateRequest) {
        if (!emailVerificationService.isEmailVerified(email)) {
            throw new EmailNotVerifiedException();
        }

        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        if (user.getUserType() != UserType.LOCAL) {
            throw new PasswordChangeNotAllowedException();
        }

        String encodedNewPassword = passwordEncoder.encode(passwordUpdateRequest.getPassword());

        user.updatePassword(encodedNewPassword);
    }

    @Transactional
    public void updatePassword(AuthUser authUser, PasswordUpdateRequest passwordUpdateRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        String encodedPassword = passwordEncoder.encode(passwordUpdateRequest.getPassword());

        user.updatePassword(encodedPassword);

        refreshTokenService.deleteRefreshToken(authUser.getEmail());
    }

    public void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException();
        }
    }
}
