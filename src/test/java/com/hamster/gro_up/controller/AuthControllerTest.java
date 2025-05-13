package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.CustomAccessDeniedHandler;
import com.hamster.gro_up.config.CustomAuthenticationEntryPoint;
import com.hamster.gro_up.config.CustomOAuth2SuccessHandler;
import com.hamster.gro_up.config.SecurityConfig;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.exception.auth.InvalidEmailVerificationTokenException;
import com.hamster.gro_up.exception.user.DuplicateUserException;
import com.hamster.gro_up.service.AuthService;
import com.hamster.gro_up.service.CustomOAuth2UserService;
import com.hamster.gro_up.service.EmailVerificationService;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void signup_success() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("test@test.com", "password");
        TokenResponse token = new TokenResponse("token");
        given(authService.signup(any(SignupRequest.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.token").value(token.getToken()));

    }

    @Test
    @DisplayName("로그인에 성공하면 토큰을 반환한다")
    void signin_success() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com", "password");
        TokenResponse token = new TokenResponse("token");
        given(authService.signin(any(SigninRequest.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.token").value(token.getToken()));
    }

    @Test
    @DisplayName("필수값이 누락된 회원가입 요청 시 예외가 발생한다")
    void signup_fail_validation() throws Exception {
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
                        .param("email", email)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 코드 요청 시 이미 가입된 이메일이면 예외가 발생한다")
    void sendVerificationCode_fail_duplicate() throws Exception {
        // given
        String email = "test@example.com";
        doThrow(new DuplicateUserException("이미 가입된 이메일입니다."))
                .when(emailVerificationService).sendVerificationCode(email);

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-request")
                        .param("email", email)
                        .with(csrf()))
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
                        .param("code", code)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("이메일 인증 코드 검증에 실패하면 예외가 발생한다")
    void checkVerificationCode_fail_invalid() throws Exception {
        // given
        String email = "test@example.com";
        String code = "wrongcode";
        doThrow(new InvalidEmailVerificationTokenException())
                .when(emailVerificationService).verifyCode(email, code);

        // when & then
        mockMvc.perform(post("/api/auth/email/verify-check")
                        .param("email", email)
                        .param("code", code)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("인증 코드가 유효하지 않습니다."));
    }
}