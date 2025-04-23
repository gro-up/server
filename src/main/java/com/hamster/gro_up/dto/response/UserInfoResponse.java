package com.hamster.gro_up.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfoResponse {
    private final Long userId;
    private final String name;
    private final String email;
}
