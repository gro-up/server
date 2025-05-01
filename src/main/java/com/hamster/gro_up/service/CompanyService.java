package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyListResponse;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.company.CompanyNotFoundException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.CompanyRepository;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyResponse findCompany(AuthUser authUser, Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

        validateOwner(authUser, company);

        return CompanyResponse.from(company);
    }

    public CompanyListResponse findAllCompany(AuthUser authUser) {
        List<Company> companyList = companyRepository.findByUserId(authUser.getId());
        List<CompanyResponse> responseList = companyList.stream().map(CompanyResponse::from).toList();
        return CompanyListResponse.of(responseList);
    }

    @Transactional
    public CompanyResponse createCompany(AuthUser authUser, CompanyCreateRequest companyCreateRequest) {

        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

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

        Company company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

        validateOwner(authUser, company);

        company.update(companyUpdateRequest.getCompanyName(),
                companyUpdateRequest.getPosition(),
                companyUpdateRequest.getLocation(),
                companyUpdateRequest.getUrl()
        );
    }

    @Transactional
    public void deleteCompany(AuthUser authUser, Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

        validateOwner(authUser, company);

        companyRepository.delete(company);
    }

    public void validateOwner(AuthUser authUser, Company company) {
        if (!company.getUser().getId().equals(authUser.getId())) {
            throw new ForbiddenException("해당 리소스에 접근할 권한이 없습니다.");
        }
    }

}
