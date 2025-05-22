package com.hamster.gro_up.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.UserType;
import com.hamster.gro_up.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = jwtUtil.substringToken(authorizationHeader);
            try{
                Claims claims = jwtUtil.extractClaims(jwt);
                Long userId = Long.valueOf(claims.getSubject());
                String email = claims.get("email", String.class);
                Role role = Role.of(claims.get("role", String.class));
                UserType userType = UserType.of(claims.get("userType", String.class));

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    AuthUser authUser = AuthUser.builder().id(userId).email(email).role(role).userType(userType).build();

                    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }catch (SecurityException | MalformedJwtException e) {
                log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않는 JWT 서명입니다.");
                return;
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token, 만료된 JWT token 입니다.", e);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                ApiResponse<Void> errorResponse = ApiResponse.of(
                        1000, // 비즈니스 에러 코드
                        HttpStatus.UNAUTHORIZED,
                        "만료된 Access Token 입니다.",
                        null
                );

                String body = objectMapper.writeValueAsString(errorResponse);

                response.getWriter().write(body);
                return;
            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
                return;
            } catch (Exception e) {
                log.error("Internal server error", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
