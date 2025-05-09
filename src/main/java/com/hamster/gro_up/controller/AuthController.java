package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.service.AuthService;
import com.hamster.gro_up.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 및 인가", description = "인증 및 인가 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

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

    @Operation(summary = "이메일 인증 요청")
    @PostMapping("/email/verify-request")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestParam String email) {
        emailVerificationService.sendVerificationCode(email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "이메일 인증 코드 검증")
    @PostMapping("/email/verify-check")
    public ResponseEntity<ApiResponse<Void>> checkVerificationCode(@RequestParam String email, @RequestParam String code) {
        emailVerificationService.verifyCode(email, code);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
