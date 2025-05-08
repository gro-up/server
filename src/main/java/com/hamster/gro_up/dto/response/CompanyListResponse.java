package com.hamster.gro_up.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CompanyListResponse {
    List<CompanyResponse> companyList;

    public static CompanyListResponse of(List<CompanyResponse> companyList) {
        return new CompanyListResponse(companyList);
    }
}
