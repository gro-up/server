package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.RetrospectCreateRequest;
import com.hamster.gro_up.dto.request.RetrospectUpdateRequest;
import com.hamster.gro_up.dto.response.RetrospectListResponse;
import com.hamster.gro_up.dto.response.RetrospectResponse;
import com.hamster.gro_up.service.RetrospectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/retrospects")
@RestController
public class RetrospectController {

    private final RetrospectService retrospectService;

    @Operation(summary = "회고 단건 조회")
    @GetMapping("/{retrospectId}")
    public ResponseEntity<ApiResponse<RetrospectResponse>> findRetrospect(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long retrospectId) {
        RetrospectResponse response = retrospectService.findRetrospect(authUser, retrospectId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "해당 사용자의 모든 회고 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<RetrospectListResponse>> findAllRetrospects(@AuthenticationPrincipal AuthUser authUser) {
        RetrospectListResponse response = retrospectService.findAllRetrospects(authUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "회고 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<RetrospectResponse>> createRetrospect(@AuthenticationPrincipal AuthUser authUser,
                                                                           @Valid @RequestBody RetrospectCreateRequest request) {
        RetrospectResponse response = retrospectService.createRetrospect(authUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, response));
    }

    @Operation(summary = "회고 수정")
    @PutMapping("/{retrospectId}")
    public ResponseEntity<ApiResponse<Void>> updateRetrospect(@AuthenticationPrincipal AuthUser authUser,
                                                              @PathVariable Long retrospectId,
                                                              @Valid @RequestBody RetrospectUpdateRequest request) {
        retrospectService.updateRetrospect(authUser, retrospectId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "회고 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteRetrospect(@AuthenticationPrincipal AuthUser authUser, Long retrospectId) {
        retrospectService.deleteRetrospect(authUser, retrospectId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
