package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.*;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.PasswordCheckRequest;
import com.hamster.gro_up.dto.request.PasswordUpdateRequest;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.exception.auth.ExpiredTokenException;
import com.hamster.gro_up.exception.auth.InvalidCredentialsException;
import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.auth.TokenTypeMismatchException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.service.AuthService;
import com.hamster.gro_up.service.CustomOAuth2UserService;
import com.hamster.gro_up.service.EmailVerificationService;
import com.hamster.gro_up.util.CookieUtil;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class})
@Import({SecurityConfig.class, JwtUtil.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("회원가입에 성공하면 토큰을 반환한다")
    void signUp_success() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("test@test.com", "password");
        TokenResponse token = new TokenResponse("Access Token", "Refresh Token");
        given(authService.signUp(any(SignupRequest.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").value(token.getAccessToken()))
                .andExpect(cookie().value(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "Refresh Token"));
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰을 반환한다")
    void signIn_success() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com", "password");
        TokenResponse token = new TokenResponse("Access Token", "Refresh Token");
        given(authService.signIn(any(SigninRequest.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").value(token.getAccessToken()))
                .andExpect(cookie().value(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "Refresh Token"));
    }

    @Test
    @DisplayName("필수값이 누락된 회원가입 요청 시 400을 반환한다")
    void signUp_fail_validation() throws Exception {
        // given
        SignupRequest invalidRequest = new SignupRequest("", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 코드 요청에 성공한다")
    void sendVerificationCode_success() throws Exception {
        // given
        String email = "test@example.com";

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-request")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 코드 요청 시 이미 가입된 이메일이면 400을 반환한다")
    void sendVerificationCode_fail_duplicate() throws Exception {
        // given
        String email = "test@example.com";
        doThrow(new DuplicateUserException("이미 가입된 이메일입니다."))
                .when(emailVerificationService).sendVerificationCode(email);

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-request")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 인증 코드 검증에 성공한다")
    void checkVerificationCode_success() throws Exception {
        // given
        String email = "test@example.com";
        String code = "123456";

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-check")
                        .param("email", email)
                        .param("code", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 코드 검증에 실패하면 400을 반환한다")
    void checkVerificationCode_fail_invalid() throws Exception {
        // given
        String email = "test@example.com";
        String code = "wrongcode";
        doThrow(new InvalidEmailVerificationTokenException())
                .when(emailVerificationService).verifyCode(email, code);

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-check")
                        .param("email", email)
                        .param("code", code))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("인증 코드가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("로그아웃에 성공하면 200 OK와 쿠키 만료 응답을 반환한다")
    void logout_success() throws Exception {
        // given
        String refreshToken = "refresh-token-value";
        // 쿠키 세팅
        MockCookie refreshCookie = new MockCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(cookie().maxAge(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, 0)); // 쿠키 만료 확인

        // verify 서비스 호출
        verify(authService).signOut(refreshToken);
    }

    @Test
    @DisplayName("Refresh Token 이 없으면 로그아웃 시 400 을 반환한다")
    void logout_fail_noRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Refresh Token 이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("정상적인 Refresh Token 으로 토큰 재발급에 성공한다")
    void reissue_success() throws Exception {
        // given
        String refreshToken = "refresh-token-value";
        TokenResponse tokenResponse = new TokenResponse("newAccessToken", "newRefreshToken");

        MockCookie refreshCookie = new MockCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);

        given(authService.reissueAccessToken(refreshToken)).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").value("newAccessToken"))
                .andExpect(cookie().value(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, "newRefreshToken"));
    }

    @Test
    @DisplayName("Refresh Token 이 없으면 토큰 재발급 시 400 을 반환한다")
    void reissue_fail_noRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Refresh Token 이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("만료된 Refresh Token 으로 로그아웃 시 401을 반환된다")
    void logout_fail_expiredToken() throws Exception {
        // given
        String refreshToken = "expiredRefreshToken";
        MockCookie refreshCookie = new MockCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        doThrow(new ExpiredTokenException()).when(authService).signOut(refreshToken);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("만료된 토큰입니다."));
    }

    @Test
    @DisplayName("잘못된 타입의 Refresh Token 으로 로그아웃 시 401을 반환된다")
    void logout_fail_invalidTokenType() throws Exception {
        // given
        String refreshToken = "invalidTypeToken";
        MockCookie refreshCookie = new MockCookie(CookieUtil.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        doThrow(new TokenTypeMismatchException()).when(authService).signOut(refreshToken);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token type 이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("계정 삭제에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void deleteAccount_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/auth/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).deleteAccount(any(AuthUser.class));
    }

    @Test
    @DisplayName("계정 삭제 시 해당 사용자가 없으면 404를 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void deleteAccount_userNotFound() throws Exception {
        // given
        doThrow(new UserNotFoundException()).when(authService).deleteAccount(any(AuthUser.class));

        // when & then
        mockMvc.perform(delete("/api/auth/account"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("비밀번호 검증에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void checkPassword_success() throws Exception {
        // given
        PasswordCheckRequest request = new PasswordCheckRequest("password123");

        // when & then
        mockMvc.perform(post("/api/auth/password-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).checkPassword(any(AuthUser.class), any(PasswordCheckRequest.class));
    }

    @Test
    @DisplayName("비밀번호가 올바르지 않으면 401을 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void checkPassword_fail_wrongPassword() throws Exception {
        // given
        PasswordCheckRequest request = new PasswordCheckRequest("wrongPassword");
        doThrow(new InvalidCredentialsException()).when(authService).checkPassword(any(AuthUser.class), any(PasswordCheckRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/password-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("비밀번호 변경에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void updatePassword_success() throws Exception {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest("new password");

        // when & then
        mockMvc.perform(post("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).updatePassword(any(AuthUser.class), any(PasswordUpdateRequest.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청에 성공한다")
    void resetRequest_success() throws Exception {
        // given
        String email = "ham@example.com";

        // when & then
        mockMvc.perform(post("/api/auth/reset-request")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).requestPasswordReset(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 재설정 요청 시 404를 반환한다")
    void resetRequest_fail_emailNotFound() throws Exception {
        // given
        String email = "notfound@example.com";
        doThrow(new UserNotFoundException()).when(authService).requestPasswordReset(email);

        // when & then
        mockMvc.perform(post("/api/auth/reset-request")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("비밀번호 재설정에 성공한다")
    void reset_success() throws Exception {
        // given
        String token = "reset-token";
        PasswordUpdateRequest request = new PasswordUpdateRequest("newPassword123");

        // when & then
        mockMvc.perform(post("/api/auth/reset-password")
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).resetPassword(eq(token), any(PasswordUpdateRequest.class));
    }

    @Test
    @DisplayName("만료된 토큰으로 비밀번호 재설정 시 401을 반환한다")
    void reset_fail_expiredToken() throws Exception {
        // given
        String token = "expired-token";
        PasswordUpdateRequest request = new PasswordUpdateRequest("newPassword123");
        doThrow(new ExpiredTokenException()).when(authService).resetPassword(eq(token), any(PasswordUpdateRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/reset-password")
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}