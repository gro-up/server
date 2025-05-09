package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.auth.InvalidCredentialsException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.UserRepository;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

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
        signupRequest = new SignupRequest("test@example.com", "test", "password");
        signinRequest = new SigninRequest("test@example.com", "testpw");

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
    @DisplayName("회원가입에 성공하면 토큰을 반환한다")
    void signup_success() {
        // given
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(Role.class))).willReturn("token");
        given(emailVerificationService.isEmailVerified(signupRequest.getEmail())).willReturn(true);

        // when
        TokenResponse response = authService.signup(signupRequest);

        // then
        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
    void signup_fail_duplicatedEmail() {
        // given
        String duplicatedEmail = "test@example.com";
        given(userRepository.existsByEmail(duplicatedEmail)).willReturn(true);

        // when & then
        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> authService.signup(signupRequest));
        assertThat(exception.getMessage()).isEqualTo("중복된 사용자가 존재합니다.");
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

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void signin_fail_userNotFound() {
        // given
        SigninRequest request = new SigninRequest("notfound@example.com", "test");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.signin(request));
        assertThat(exception.getMessage()).isEqualTo("해당 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 시 예외가 발생한다")
    void signin_fail_invalidPassword() {
        // given
        SigninRequest request = new SigninRequest("test@example.com", "wrongPassword");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

        // when & then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.signin(request));
        assertThat(exception.getMessage()).isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
