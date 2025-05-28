package com.hamster.gro_up.dto.response;

import com.hamster.gro_up.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponse {
    private Long userId;
    private String email;
    private String imageUrl;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getImageUrl());
    }
}
