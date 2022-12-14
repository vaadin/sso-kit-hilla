package com.example.application.data.endpoint;

import java.util.Optional;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.example.application.data.entity.User;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

import dev.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class UserEndpoint {

    private final AuthenticationContext authenticationContext;

    public UserEndpoint(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<User> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(OidcUser.class).map(oidcUser -> {
            User user = new User();
            user.setName(oidcUser.getUserInfo().getClaimAsString("name"));
            user.setUsername(oidcUser.getUserInfo()
                    .getClaimAsString("preferred_username"));
            return user;
        });
    }
}
