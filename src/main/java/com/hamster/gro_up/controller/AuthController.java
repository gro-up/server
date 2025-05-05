package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<TokenResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        TokenResponse response = authService.signup(signupRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/siginin")
    public ResponseEntity<ApiResponse<TokenResponse>> signin(@Valid @RequestBody SigninRequest signinRequest) {
        TokenResponse response = authService.signin(signinRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
