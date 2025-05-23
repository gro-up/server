package com.hamster.gro_up.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.CustomOAuth2User;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.UserType;
import com.hamster.gro_up.util.CookieUtil;
import com.hamster.gro_up.util.JwtUtil;
import com.hamster.gro_up.util.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

        String accessToken = jwtUtil.createToken(TokenType.ACCESS, UserType.OAUTH, userId, email, Role.ROLE_USER);
        String refreshToken = jwtUtil.createToken(TokenType.REFRESH, UserType.OAUTH, userId, email, Role.ROLE_USER);

        ApiResponse<String> apiResponse = ApiResponse.of(HttpStatus.OK, accessToken);

        // Refresh Token 은 Cookie 에, Access Token 은 Body 에 담음
        CookieUtil.addRefreshTokenCookie(response, refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
