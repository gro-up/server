package com.hamster.gro_up.config;

import com.hamster.gro_up.dto.CustomOAuth2User;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = customOAuth2User.getId();
        String email = customOAuth2User.getAttribute("email");
        String name = customOAuth2User.getAttribute("name");

        String token = jwtUtil.createToken(userId, email, name, Role.ROLE_USER);

        response.setContentType("application/json");
        response.getWriter().write(token);
    }
}
