package com.example.application.data.endpoint;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.example.application.data.entity.User;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class UserEndpoint {

    public Optional<User> getAuthenticatedUser() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(p -> (OidcUser) p)
                .map(ou -> {
                    User user = new User();
                    user.setName(ou.getUserInfo().getClaimAsString("name"));
                    user.setUsername(ou.getUserInfo().getClaimAsString("preferred_username"));
                    return user;
                });
    }
}
