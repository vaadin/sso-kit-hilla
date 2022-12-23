package com.example.application.endpoints.helloworld;

import dev.hilla.Endpoint;
import dev.hilla.sso.endpoint.AuthEndpoint;
import jakarta.annotation.security.PermitAll;

@Endpoint
@PermitAll
public class HelloWorldEndpoint {

    private final AuthEndpoint authEndpoint;

    public HelloWorldEndpoint(AuthEndpoint authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String sayHello(String name) {
        if (name.isEmpty()) {
            return "Hello "
                    + authEndpoint.getAuthenticatedUser().get().getName();
        } else {
            return "Hello " + name;
        }
    }
}
