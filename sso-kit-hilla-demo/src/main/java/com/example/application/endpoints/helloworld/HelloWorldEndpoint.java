package com.example.application.endpoints.helloworld;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import dev.hilla.Endpoint;
import dev.hilla.sso.endpoint.AuthEndpoint;
import jakarta.annotation.security.PermitAll;

@Endpoint
@PermitAll
public class HelloWorldEndpoint {

    public String sayHello(String name) {
        if (name.isEmpty()) {
            return "Hello " + AuthEndpoint.getOidcUser()
                    .map(OidcUser::getFullName).orElse("anonymous");
        } else {
            return "Hello " + name;
        }
    }
}
