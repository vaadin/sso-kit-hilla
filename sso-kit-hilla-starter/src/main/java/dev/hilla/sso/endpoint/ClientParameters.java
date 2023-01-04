/*-
 * Copyright (C) 2022-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
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
