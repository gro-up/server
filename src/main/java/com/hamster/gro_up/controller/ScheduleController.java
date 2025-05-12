package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ScheduleCreateRequest;
import com.hamster.gro_up.dto.request.ScheduleUpdateRequest;
import com.hamster.gro_up.dto.response.ScheduleListResponse;
import com.hamster.gro_up.dto.response.ScheduleResponse;
import com.hamster.gro_up.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일정", description = "일정 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "일정 단건 조회")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> findSchedule(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long scheduleId) {
        ScheduleResponse response = scheduleService.findSchedule(authUser, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "해당 사용자의 모든 일정 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleListResponse>> findAllSchedules(@AuthenticationPrincipal AuthUser authUser) {
        ScheduleListResponse response = scheduleService.findAllSchedules(authUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "일정 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@AuthenticationPrincipal AuthUser authUser, @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        ScheduleResponse response = scheduleService.createSchedule(authUser, scheduleCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, response));
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> updateSchedule(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable long scheduleId,
                                                            @RequestBody ScheduleUpdateRequest scheduleUpdateRequest) {
        scheduleService.updateSchedule(authUser, scheduleId, scheduleUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@AuthenticationPrincipal AuthUser authUser, @PathVariable long scheduleId) {
        scheduleService.deleteSchedule(authUser, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
