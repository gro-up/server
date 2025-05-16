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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private RefreshTokenService refreshTokenService;

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
        signupRequest = new SignupRequest("test@example.com", "password");
        signinRequest = new SigninRequest("test@example.com", "testpw");

        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("회원가입에 성공하면 토큰을 반환한다")
    void signUp_success() {
        // given
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(eq(TokenType.ACCESS), anyLong(), anyString(), any(Role.class))).willReturn("Access Token");
        given(jwtUtil.createToken(eq(TokenType.REFRESH), anyLong(), anyString(), any(Role.class))).willReturn("Refresh Token");
        given(emailVerificationService.isEmailVerified(signupRequest.getEmail())).willReturn(true);

        // when
        TokenResponse response = authService.signUp(signupRequest);

        // then
        assertThat(response.getAccessToken()).isEqualTo("Access Token");
        assertThat(response.getRefreshToken()).isEqualTo("Refresh Token");
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
    void signUp_fail_duplicatedEmail() {
        // given
        String duplicatedEmail = "test@example.com";
        given(userRepository.existsByEmail(duplicatedEmail)).willReturn(true);

        // when & then
        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> authService.signUp(signupRequest));
        assertThat(exception.getMessage()).isEqualTo("중복된 사용자가 존재합니다.");
    }


    @Test
    @DisplayName("로그인에 성공하면 토큰을 반환한다")
    void signIn_success() {
        // given
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(eq(TokenType.ACCESS), anyLong(), anyString(), any(Role.class))).willReturn("Access Token");
        given(jwtUtil.createToken(eq(TokenType.REFRESH), anyLong(), anyString(), any(Role.class))).willReturn("Refresh Token");

        // when
        TokenResponse response = authService.signIn(signinRequest);

        // then
        assertThat(response.getAccessToken()).isEqualTo("Access Token");
        assertThat(response.getRefreshToken()).isEqualTo("Refresh Token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void signIn_fail_userNotFound() {
        // given
        SigninRequest request = new SigninRequest("notfound@example.com", "test");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.signIn(request));
        assertThat(exception.getMessage()).isEqualTo("해당 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 시 예외가 발생한다")
    void signIn_fail_invalidPassword() {
        // given
        SigninRequest request = new SigninRequest("test@example.com", "wrongPassword");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

        // when & then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.signIn(request));
        assertThat(exception.getMessage()).isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("정상적인 Refresh Token 으로 로그아웃에 성공하면 토큰이 삭제된다")
    void signOut_success() {
        // given
        String refreshToken = "validRefreshToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.REFRESH.name());
        given(jwtUtil.getEmail(refreshToken)).willReturn("test@example.com");
        given(refreshTokenService.existsByEmail("test@example.com")).willReturn(true);

        // when
        authService.signOut(refreshToken);

        // then
        verify(refreshTokenService).deleteRefreshToken("test@example.com");
    }

    @Test
    @DisplayName("만료된 Refresh Token 으로 로그아웃 시 예외가 발생한다")
    void signOut_fail_expiredToken() {
        // given
        String refreshToken = "expiredRefreshToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(true);

        // when & then
        assertThrows(ExpiredTokenException.class, () -> authService.signOut(refreshToken));
    }

    @Test
    @DisplayName("Refresh 타입이 아닌 토큰으로 로그아웃 시 예외가 발생한다")
    void signOut_fail_invalidTokenType() {
        // given
        String refreshToken = "accessToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.ACCESS.name());

        // when & then
        assertThrows(InvalidTokenException.class, () -> authService.signOut(refreshToken));
    }

    @Test
    @DisplayName("이미 로그아웃된 사용자(토큰 없음)로 로그아웃 시 예외가 발생한다")
    void signOut_fail_alreadyLoggedOut() {
        // given
        String refreshToken = "validRefreshToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.REFRESH.name());
        given(jwtUtil.getEmail(refreshToken)).willReturn("test@example.com");
        given(refreshTokenService.existsByEmail("test@example.com")).willReturn(false);

        // when & then
        assertThrows(InvalidTokenException.class, () -> authService.signOut(refreshToken));
    }

    @Test
    @DisplayName("정상적인 Refresh Token 으로 Access Token 재발급에 성공한다")
    void reissueAccessToken_success() {
        // given
        String refreshToken = "validRefreshToken";
        AuthUser authUser = mock(AuthUser.class);
        String email = "test@example.com";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.REFRESH.name());
        given(jwtUtil.getAuthUserFromToken(refreshToken)).willReturn(authUser);
        given(authUser.getEmail()).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(refreshToken);
        given(authUser.getId()).willReturn(1L);
        given(authUser.getRole()).willReturn(Role.ROLE_USER);
        given(jwtUtil.createToken(eq(TokenType.ACCESS), anyLong(), anyString(), any(Role.class))).willReturn(newAccessToken);
        given(jwtUtil.createToken(eq(TokenType.REFRESH), anyLong(), anyString(), any(Role.class))).willReturn(newRefreshToken);

        // when
        TokenResponse response = authService.reissueAccessToken(refreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        verify(refreshTokenService).deleteRefreshToken(email);
        verify(refreshTokenService).saveRefreshToken(newRefreshToken, email);
    }

    @Test
    @DisplayName("Refresh Token이 null이면 예외가 발생한다")
    void reissueAccessToken_fail_nullToken() {
        // when & then
        assertThrows(InvalidTokenException.class, () -> authService.reissueAccessToken(null));
    }

    @Test
    @DisplayName("만료된 Refresh Token 으로 재발급 시 예외가 발생한다")
    void reissueAccessToken_fail_expiredToken() {
        // given
        String refreshToken = "expiredRefreshToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(true);

        // when & then
        assertThrows(ExpiredTokenException.class, () -> authService.reissueAccessToken(refreshToken));
    }

    @Test
    @DisplayName("Refresh 타입이 아닌 토큰으로 재발급 시 예외가 발생한다")
    void reissueAccessToken_fail_invalidTokenType() {
        // given
        String refreshToken = "accessToken";
        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.ACCESS.name());

        // when & then
        assertThrows(TokenTypeMismatchException.class, () -> authService.reissueAccessToken(refreshToken));
    }

    @Test
    @DisplayName("저장된 Refresh Token 이 없으면 예외가 발생한다")
    void reissueAccessToken_fail_tokenNotFound() {
        // given
        String refreshToken = "validRefreshToken";
        AuthUser authUser = mock(AuthUser.class);
        String email = "test@example.com";

        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.REFRESH.name());
        given(jwtUtil.getAuthUserFromToken(refreshToken)).willReturn(authUser);
        given(authUser.getEmail()).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(null);

        // when & then
        assertThrows(TokenNotFoundException.class, () -> authService.reissueAccessToken(refreshToken));
    }

    @Test
    @DisplayName("저장된 Refresh Token 과 요청값이 다르면 예외가 발생한다")
    void reissueAccessToken_fail_tokenMismatch() {
        // given
        String refreshToken = "requestToken";
        String storedToken = "storedToken";
        AuthUser authUser = mock(AuthUser.class);
        String email = "test@example.com";

        given(jwtUtil.isExpired(refreshToken)).willReturn(false);
        given(jwtUtil.getTokenType(refreshToken)).willReturn(TokenType.REFRESH.name());
        given(jwtUtil.getAuthUserFromToken(refreshToken)).willReturn(authUser);
        given(authUser.getEmail()).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(storedToken);

        // when & then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> authService.reissueAccessToken(refreshToken));
        assertThat(exception.getMessage()).isEqualTo("Refresh Token 이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("계정 삭제에 성공하면 토큰 삭제 및 유저 삭제가 호출된다")
    void deleteAccount_success() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", Role.ROLE_USER);
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));

        // when
        authService.deleteAccount(authUser);

        // then
        verify(userRepository).findById(authUser.getId());
        verify(refreshTokenService).deleteRefreshToken(authUser.getEmail());
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("존재하지 않는 유저라면 예외가 발생한다")
    void deleteAccount_userNotFound() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", Role.ROLE_USER);
        given(userRepository.findById(authUser.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> authService.deleteAccount(authUser));
        verify(userRepository).findById(authUser.getId());
        verifyNoMoreInteractions(userRepository, refreshTokenService);
    }
}
