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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@AuthenticationPrincipal AuthUser authUser,
                                                                        @Valid @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        ScheduleResponse response = scheduleService.createSchedule(authUser, scheduleCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, response));
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> updateSchedule(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable Long scheduleId,
                                                            @RequestBody ScheduleUpdateRequest scheduleUpdateRequest) {
        scheduleService.updateSchedule(authUser, scheduleId, scheduleUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(authUser, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "날짜 범위별 일정 조회")
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<ScheduleListResponse>> findSchedulesByDateRange(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate start,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate end
    ) {
        ScheduleListResponse response = scheduleService.findSchedulesInRange(authUser, start, end);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "정확한 기업명으로 일정 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ScheduleListResponse>> findSchedulesByCompanyName(
            @AuthenticationPrincipal AuthUser authUser, @RequestParam String companyName) {
        ScheduleListResponse response = scheduleService.findSchedulesByCompanyName(authUser, companyName);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
