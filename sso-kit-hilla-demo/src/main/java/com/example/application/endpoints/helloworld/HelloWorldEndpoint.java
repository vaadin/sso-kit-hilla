package com.example.application.endpoints.helloworld;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import dev.hilla.sso.endpoint.AuthEndpoint;
import jakarta.annotation.security.RolesAllowed;

@Endpoint
@RolesAllowed("ROLE_USER")
public class HelloWorldEndpoint {

    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello "
                    + new AuthEndpoint().getAuthenticatedUser().get().getName();
        } else {
            return "Hello " + name;
        }
    }
}
