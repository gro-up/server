package com.hamster.gro_up.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CompanyUpdateRequest {

    private String companyName;

    private String position;

    private String url;

    private String location;
}
