package com.hamster.gro_up.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Builder
    public User(Long id, String email, String password, Role role, UserType userType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.userType = userType;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
