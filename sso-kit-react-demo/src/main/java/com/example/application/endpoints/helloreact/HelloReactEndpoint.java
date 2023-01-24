package com.example.application.endpoints.helloreact;

import javax.annotation.security.PermitAll;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;

@Endpoint
@PermitAll
public class HelloReactEndpoint {

    @Nonnull
    public String sayHello(@Nonnull String name) {
        if (name.isEmpty()) {
            return "Hello stranger";
        } else {
            return "Hello " + name;
        }
    }
}
