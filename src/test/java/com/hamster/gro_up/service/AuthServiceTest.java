package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.repository.UserRepository;
import com.hamster.gro_up.util.JwtUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private SigninRequest signinRequest;
    private User user;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest("test@gmail.com", "test", "password");
        signinRequest = new SigninRequest("test@email.com", "testpw");

        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .name("ham")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("회원가입에 성공하면 토큰을 반환한다.")
    void signup_success() {
        // given
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(Role.class))).willReturn("token");

        // when
        TokenResponse response = authService.signup(signupRequest);

        // then
        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰을 반환한다")
    void signin_success() {
        // given
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(Role.class))).willReturn("token");

        // when
        TokenResponse response = authService.signin(signinRequest);

        // then
        assertThat(response.getToken()).isEqualTo("token");
    }

}