package com.hamster.gro_up.config;

import com.hamster.gro_up.dto.AuthUser;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockAuthUserSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthUser authUser = AuthUser.builder().id(customUser.userId()).email(customUser.email()).role(customUser.role()).build();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);

        context.setAuthentication(authenticationToken);
        return context;
    }
}
