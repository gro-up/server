package com.hamster.gro_up.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    private final Long id;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Long id, Map<String, Object> attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
