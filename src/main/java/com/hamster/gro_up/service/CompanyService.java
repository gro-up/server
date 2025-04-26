package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.repository.CompanyRepository;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyResponse findCompany(AuthUser authUser, Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("해당 기업을 찾을 수 없습니다."));

        validateOwner(authUser, company);

        return CompanyResponse.from(company);
    }

    @Transactional
    public CompanyResponse createCompany(AuthUser authUser, CompanyCreateRequest companyCreateRequest) {

        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        Company company = Company.builder()
                .user(user)
                .companyName(companyCreateRequest.getCompanyName())
                .position(companyCreateRequest.getPosition())
                .location(companyCreateRequest.getLocation())
                .url(companyCreateRequest.getUrl())
                .build();

        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }

    @Transactional
    public void updateCompany(AuthUser authUser, Long companyId, CompanyUpdateRequest companyUpdateRequest) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("해당 기업을 찾을 수 없습니다."));

        validateOwner(authUser, company);

        company.update(companyUpdateRequest.getCompanyName(),
                companyUpdateRequest.getPosition(),
                companyUpdateRequest.getLocation(),
                companyUpdateRequest.getUrl()
        );
    }

    @Transactional
    public void deleteCompany(AuthUser authUser, Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("해당 기업을 찾을 수 없습니다."));

        validateOwner(authUser, company);

        companyRepository.delete(company);
    }

    public void validateOwner(AuthUser authUser, Company company) {
        if (!company.getUser().getId().equals(authUser.getId())) {
            throw new RuntimeException("잘못된 요청입니다.");
        }
    }

}
