package com.example.application.endpoints.helloworld;

import javax.annotation.security.PermitAll;

import com.example.application.data.endpoint.UserEndpoint;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;

@Endpoint
@PermitAll
public class HelloWorldEndpoint {

    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello " + new UserEndpoint().getAuthenticatedUser().get().getName();
        } else {
            return "Hello " + name;
        }
    }
}
