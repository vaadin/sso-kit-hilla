package com.example.application.endpoints.helloworld;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import dev.hilla.sso.endpoint.AuthEndpoint;
import dev.hilla.sso.starter.SingleSignOnProperties;
import jakarta.annotation.security.RolesAllowed;

@Endpoint
@RolesAllowed("ROLE_USER")
public class HelloWorldEndpoint {

    private final SingleSignOnProperties properties;

    public HelloWorldEndpoint(SingleSignOnProperties properties) {
        this.properties = properties;
    }

    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello " + new AuthEndpoint(properties)
                    .getAuthenticatedUser().get().getName();
        } else {
            return "Hello " + name;
        }
    }
}
