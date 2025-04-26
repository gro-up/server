package com.hamster.gro_up.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignupRequest {

    private String email;

    private String name;

    private String password;
}
