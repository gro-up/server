package com.hamster.gro_up.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignupRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String password;
}
