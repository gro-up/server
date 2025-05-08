package com.hamster.gro_up.dto.response;

import com.hamster.gro_up.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CompanyResponse {

    private Long companyId;

    private String companyName;

    private String position;

    private String url;

    private String location;

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getCompanyName(),
                company.getPosition(),
                company.getUrl(),
                company.getLocation());
    }
}
