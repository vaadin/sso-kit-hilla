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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import dev.hilla.EndpointSubscription;
import dev.hilla.sso.starter.bclogout.FluxHolder;
import jakarta.annotation.security.PermitAll;

@Endpoint
@AnonymousAllowed
public class AuthEndpoint {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final int ROLE_PREFIX_LENGTH = ROLE_PREFIX.length();
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AuthEndpoint.class);

    private final ClientParameters clientParameters;
    private final FluxHolder fluxHolder;

    public AuthEndpoint(ClientParameters clientParameters,
            FluxHolder fluxHolder) {
        this.clientParameters = clientParameters;
        this.fluxHolder = fluxHolder;
    }

    // This method is not really useful since the configuration is already
    // available on the client. It exists to let Hilla translate
    // ClientParameters to TypeScript and get code completion and type safety.
    public ClientParameters getClientParameters() {
        return clientParameters;
    }

    public Optional<User> getAuthenticatedUser() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(OidcUser.class::isInstance).map(p -> (OidcUser) p)
                .map(ou -> {
                    User user = new User();
                    user.setName(ou.getUserInfo().getClaimAsString("name"));
                    user.setUsername(ou.getUserInfo()
                            .getClaimAsString("preferred_username"));
                    user.setRoles(ou.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .filter(a -> a.startsWith(ROLE_PREFIX))
                            .map(a -> a.substring(ROLE_PREFIX_LENGTH))
                            .collect(Collectors.toSet()));
                    return user;
                });
    }

    @PermitAll
    public EndpointSubscription<String> backChannelLogout() {
        LOGGER.debug("Client subscribed to back channel logout information");
        var principal = SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        var flux = fluxHolder.getFlux().filter(p -> {
            return Objects.equals(p, principal);
        }).map(p -> "Your session has been terminated");

        return EndpointSubscription.of(flux, () -> {
            LOGGER.debug(
                    "Client cancelled subscription to back channel logout information");
        });
    }
}
