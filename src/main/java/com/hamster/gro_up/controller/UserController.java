package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ImageUpdateRequest;
import com.hamster.gro_up.dto.response.UserResponse;
import com.hamster.gro_up.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저", description = "유저 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "본인 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> findMe(@AuthenticationPrincipal AuthUser authUser) {
        UserResponse response = userService.findUser(authUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "프로필 이미지 업로드 및 수정")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateProfileImage(@AuthenticationPrincipal AuthUser authUser, @RequestBody ImageUpdateRequest imageUpdateRequest) {
        userService.updateProfileImage(authUser, imageUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
