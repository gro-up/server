package com.hamster.gro_up.entity;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserType {
    LOCAL, OAUTH;

    public static UserType of(String userType) {
        return Arrays.stream(UserType.values())
                .filter(r -> r.name().equalsIgnoreCase(userType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효하지 않은 UserType"));
    }
}
