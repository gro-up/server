package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.CustomOAuth2SuccessHandler;
import com.hamster.gro_up.config.SecurityConfig;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.service.AuthService;
import com.hamster.gro_up.service.CustomOAuth2UserService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AuthController.class })
@Import({SecurityConfig.class, JwtUtil.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Test
    @DisplayName("회원가입에 성공하면 토큰을 반환한다")
    void signup_success() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("test@test.com", "test", "password");
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
        mockMvc.perform(post("/api/auth/siginin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.token").value(token.getToken()));
    }
}