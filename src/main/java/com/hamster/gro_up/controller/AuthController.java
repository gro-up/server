package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 및 인가", description = "인증 및 인가 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "일반 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        TokenResponse response = authService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<TokenResponse>> signin(@Valid @RequestBody SigninRequest signinRequest) {
        TokenResponse response = authService.signin(signinRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
