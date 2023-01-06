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

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class AuthEndpoint {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final int ROLE_PREFIX_LENGTH = ROLE_PREFIX.length();

    private final ClientParameters clientParameters;

    public AuthEndpoint(ClientParameters clientParameters) {
        this.clientParameters = clientParameters;
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
}
