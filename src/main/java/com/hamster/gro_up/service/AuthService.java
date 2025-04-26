package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.repository.UserRepository;
import com.hamster.gro_up.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .email(signupRequest.getEmail())
                .name(signupRequest.getName())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole());

        return new TokenResponse(bearerToken);
    }

    public TokenResponse signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new RuntimeException("가입되지 않은 유저입니다."));

        // TODO: 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환해야합니다.
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("잘못된 비밀번호입니다.");
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        return new TokenResponse(bearerToken);
    }
}
