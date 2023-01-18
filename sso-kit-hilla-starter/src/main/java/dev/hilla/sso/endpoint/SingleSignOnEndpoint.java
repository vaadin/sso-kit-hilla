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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import dev.hilla.EndpointSubscription;
import dev.hilla.Nonnull;
import static dev.hilla.sso.endpoint.SingleSignOnContext.getOidcUser;
import jakarta.annotation.security.PermitAll;

@Endpoint
public class SingleSignOnEndpoint {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SingleSignOnEndpoint.class);

    private final SingleSignOnContext context;

    public SingleSignOnEndpoint(SingleSignOnContext context) {
        this.context = context;
    }

    @AnonymousAllowed
    @Nonnull
    public SingleSignOnData allData() {
        return context.getSingleSignOnData();
    }

    @AnonymousAllowed
    public Optional<User> user() {
        return SingleSignOnContext.getOidcUser().map(User::from);
    }

    @PermitAll
    public boolean backChannelLogoutEnabled() {
        return context.isBackChannelLogoutEnabled();
    }

    @PermitAll
    @Nonnull
    public EndpointSubscription<@Nonnull String> backChannelLogoutSubscription() {
        LOGGER.debug("Client subscribed to back channel logout information");

        return EndpointSubscription.of(context.getStringFlux(), () -> {
            LOGGER.debug(
                    "Client cancelled subscription to back channel logout information");
        });
    }

    @AnonymousAllowed
    @Nonnull
    public List<@Nonnull String> registeredProviders() {
        return context.getRegisteredProviders();
    }

    @PermitAll
    @Nonnull
    public String logoutUrl() {
        return getOidcUser().flatMap(context::getLogoutUrl).orElseThrow();
    }
}
