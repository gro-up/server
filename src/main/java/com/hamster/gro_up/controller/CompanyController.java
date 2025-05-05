package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyListResponse;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기업", description = "기업 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/companies")
@RestController
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "기업 단건 조회")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> findCompany(@AuthenticationPrincipal AuthUser authUser, @PathVariable long companyId) {
        CompanyResponse response = companyService.findCompany(authUser, companyId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "해당 사용자의 모든 기업 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<CompanyListResponse>> findAllCompany(@AuthenticationPrincipal AuthUser authUser) {
        CompanyListResponse response = companyService.findAllCompany(authUser);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "기업 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody CompanyCreateRequest companyCreateRequest) {
        CompanyResponse response = companyService.createCompany(authUser, companyCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, response));
    }

    @Operation(summary = "기업 수정")
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> updateCompany(@AuthenticationPrincipal AuthUser authUser,
                                                           @PathVariable long companyId,
                                                           @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest) {
        companyService.updateCompany(authUser, companyId, companyUpdateRequest);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "기엽 삭제")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@AuthenticationPrincipal AuthUser authUser,
                                                           @PathVariable long companyId) {
        companyService.deleteCompany(authUser, companyId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
