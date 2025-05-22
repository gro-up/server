package com.hamster.gro_up.dto;

import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class AuthUser {

    private Long id;
    private String email;
    private Role role;
    private UserType userType;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));

        if (role == Role.ROLE_ADMIN) {
            authorities.add(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
        }

        return authorities;
    }
}
