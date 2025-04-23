package com.hamster.gro_up.dto;

import com.hamster.gro_up.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(Long id, String email, String name, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        if (role == Role.ROLE_ADMIN) {
            this.authorities = List.of(
                    new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()),
                    new SimpleGrantedAuthority(Role.ROLE_USER.name())
            );
        } else {
            this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
        }
    }
}
