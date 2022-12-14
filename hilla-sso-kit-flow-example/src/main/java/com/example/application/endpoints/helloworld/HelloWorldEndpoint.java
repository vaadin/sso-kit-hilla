package com.example.application.endpoints.helloworld;

import javax.annotation.security.PermitAll;

import com.vaadin.flow.spring.security.AuthenticationContext;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Endpoint
@PermitAll
public class HelloWorldEndpoint {

    private final AuthenticationContext authenticationContext;

    public HelloWorldEndpoint(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello " + authenticationContext.getAuthenticatedUser(OidcUser.class)
                    .map(AuthenticatedPrincipal::getName);
        } else {
            return "Hello " + name;
        }
    }
}
