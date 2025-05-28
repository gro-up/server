package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.PasswordCheckRequest;
import com.hamster.gro_up.dto.request.PasswordUpdateRequest;
import com.hamster.gro_up.dto.request.SigninRequest;
import com.hamster.gro_up.dto.request.SignupRequest;
import com.hamster.gro_up.dto.response.TokenResponse;
import com.hamster.gro_up.service.AuthService;
import com.hamster.gro_up.service.EmailVerificationService;
import com.hamster.gro_up.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<String>> signUp(@Valid @RequestBody SignupRequest signupRequest, HttpServletResponse servletResponse) {
        TokenResponse tokenResponse = authService.signUp(signupRequest);

        CookieUtil.addRefreshTokenCookie(servletResponse, tokenResponse.getRefreshToken());

        // body 에는 Access Token 만 담고 Refresh Token 은 쿠키에 담음
        return ResponseEntity.ok(ApiResponse.ok(tokenResponse.getAccessToken()));
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<String>> signIn(@Valid @RequestBody SigninRequest signinRequest, HttpServletResponse servletResponse) {
        TokenResponse tokenResponse = authService.signIn(signinRequest);

        CookieUtil.addRefreshTokenCookie(servletResponse, tokenResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok(tokenResponse.getAccessToken()));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse servletResponse) {
        String refreshToken = CookieUtil.extractCookie(request, "refresh");

        if (refreshToken == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.of(HttpStatus.BAD_REQUEST, "Refresh Token 이 존재하지 않습니다.", null));
        }

        authService.signOut(refreshToken);

        // refresh 쿠키 만료(삭제)
        CookieUtil.addExpiredRefreshTokenCookie(servletResponse);

        return ResponseEntity.ok(ApiResponse.ok(null));
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

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> reissue(HttpServletRequest request, HttpServletResponse servletResponse) {
        String refreshToken = CookieUtil.extractCookie(request, CookieUtil.REFRESH_TOKEN_COOKIE_NAME);

        if (refreshToken == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.of(HttpStatus.BAD_REQUEST, "Refresh Token 이 존재하지 않습니다.", null));
        }

        TokenResponse tokenResponse = authService.reissueAccessToken(refreshToken);

        // 새 Refresh Token 을 쿠키에 저장 (Refresh Token Rotation)
        CookieUtil.addRefreshTokenCookie(servletResponse, tokenResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok(tokenResponse.getAccessToken()));
    }

    @Operation(summary = "계정 삭제")
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal AuthUser authUser, HttpServletResponse servletResponse) {
        authService.deleteAccount(authUser);

        CookieUtil.addExpiredRefreshTokenCookie(servletResponse);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "비밀번호 검증")
    @PostMapping("/check-password")
    public ResponseEntity<ApiResponse<Void>> checkPassword(@AuthenticationPrincipal AuthUser authUser,
                                                           @Valid @RequestBody PasswordCheckRequest passwordCheckRequest) {
        authService.checkPassword(authUser, passwordCheckRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "비밀번호 변경")
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal AuthUser authUser,
                                                            @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        authService.updatePassword(authUser, passwordUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Void>> resetRequest(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> reset(@RequestParam String email,
                                                   @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        authService.resetPasswordWithEmail(email, passwordUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "이메일 중복 검사")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmailDuplicate(@RequestParam String email) {
        authService.checkEmailDuplicate(email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
