package com.example.application.endpoints.helloworld;

import com.example.application.data.endpoint.UserEndpoint;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;

@Endpoint
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
