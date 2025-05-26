package com.hamster.gro_up.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CompanyCreateRequest {

    @NotBlank
    private String companyName;

    private String position;

    private String url;

    private String address;

    private String addressDetail;
}
