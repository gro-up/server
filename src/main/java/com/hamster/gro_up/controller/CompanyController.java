package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/companies")
@RestController
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    public ApiResponse<CompanyResponse> findCompany(@AuthenticationPrincipal AuthUser authUser, @PathVariable long companyId) {
        CompanyResponse response = companyService.findCompany(authUser, companyId);
        return ApiResponse.ok(response);
    }

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(@AuthenticationPrincipal AuthUser authUser, @RequestBody CompanyCreateRequest companyCreateRequest) {
        CompanyResponse response = companyService.createCompany(authUser, companyCreateRequest);
        return ApiResponse.of(HttpStatus.CREATED, response);
    }

    @PutMapping("/{companyId}")
    public ApiResponse<Void> updateCompany(@AuthenticationPrincipal AuthUser authUser,
                                         @PathVariable long companyId,
                                         @RequestBody CompanyUpdateRequest companyUpdateRequest) {
        companyService.updateCompany(authUser, companyId, companyUpdateRequest);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{companyId}")
    public ApiResponse<Void> deleteCompany(@AuthenticationPrincipal AuthUser authUser,
                                              @PathVariable long companyId) {
        companyService.deleteCompany(authUser, companyId);
        return ApiResponse.ok(null);
    }
}
