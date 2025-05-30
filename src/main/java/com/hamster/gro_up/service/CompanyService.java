package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyListResponse;
import com.hamster.gro_up.dto.response.CompanyNameListResponse;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.User;
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

        company.validateOwner(authUser.getId());

        return CompanyResponse.from(company);
    }

    public CompanyListResponse findAllCompanies(AuthUser authUser) {
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
                .address(companyCreateRequest.getAddress())
                .addressDetail(companyCreateRequest.getAddressDetail())
                .url(companyCreateRequest.getUrl())
                .build();

        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }

    @Transactional
    public void updateCompany(AuthUser authUser, Long companyId, CompanyUpdateRequest companyUpdateRequest) {
        Company company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

        company.validateOwner(authUser.getId());

        company.update(companyUpdateRequest.getCompanyName(),
                companyUpdateRequest.getPosition(),
                companyUpdateRequest.getAddress(),
                companyUpdateRequest.getAddressDetail(),
                companyUpdateRequest.getUrl()
        );
    }

    @Transactional
    public void deleteCompany(AuthUser authUser, Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

        company.validateOwner(authUser.getId());

        companyRepository.delete(company);
    }

    public CompanyNameListResponse findAllCompanyNames(AuthUser authUser) {
        List<String> companyNames = companyRepository.findAllCompanyNamesByUserId(authUser.getId());
        return CompanyNameListResponse.of(companyNames);
    }
}
