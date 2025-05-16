package com.hamster.gro_up.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.CustomOAuth2User;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.util.JwtUtil;
import com.hamster.gro_up.util.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = customOAuth2User.getId();
        String email = customOAuth2User.getAttribute("email");

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, userId, email, Role.ROLE_USER);
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, userId, email, Role.ROLE_USER);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

        ApiResponse<TokenResponse> apiResponse = ApiResponse.of(HttpStatus.OK, tokenResponse);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
