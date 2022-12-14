package com.example.application.endpoints.logout;

import javax.annotation.security.PermitAll;

import dev.hilla.Endpoint;

import com.vaadin.flow.spring.security.AuthenticationContext;

@Endpoint
@PermitAll
public class LogoutEndpoint {
    private final AuthenticationContext authenticationContext;

    public LogoutEndpoint(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void logout() {
        authenticationContext.logout();
    }
}
