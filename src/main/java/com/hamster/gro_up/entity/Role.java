package com.hamster.gro_up.entity;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Role {
    ROLE_USER, ROLE_ADMIN;

    public static Role of(String role) {
        return Arrays.stream(Role.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Role"));
    }
}
