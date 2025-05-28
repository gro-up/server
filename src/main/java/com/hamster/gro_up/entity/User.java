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

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Builder
    public User(Long id, String email, String password, String imageUrl, Role role, UserType userType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
        this.role = role;
        this.userType = userType;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
}
