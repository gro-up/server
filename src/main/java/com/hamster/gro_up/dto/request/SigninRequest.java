package com.hamster.gro_up.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SigninRequest {

    private String email;

    private String password;
}
