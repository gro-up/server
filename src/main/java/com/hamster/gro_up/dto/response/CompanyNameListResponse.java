package com.hamster.gro_up.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CompanyNameListResponse {
    private List<String> companyNameList;

    public static CompanyNameListResponse of(List<String> companyNameList) {
        return new CompanyNameListResponse(companyNameList);
    }
}
