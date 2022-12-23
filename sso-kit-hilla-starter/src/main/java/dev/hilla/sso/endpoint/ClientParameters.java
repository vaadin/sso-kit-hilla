package dev.hilla.sso.endpoint;

import org.springframework.stereotype.Component;

import dev.hilla.sso.starter.SingleSignOnProperties;

@Component
public class ClientParameters {
    private final String loginURL;

    public ClientParameters(SingleSignOnProperties properties) {
        loginURL = properties.getLoginRoute();
    }

    public String getLoginURL() {
        return loginURL;
    }
}
