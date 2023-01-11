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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import dev.hilla.sso.starter.SingleSignOnProperties;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

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

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final SingleSignOnProperties properties;

    private final FluxHolder fluxHolder;

    public AuthEndpoint(
            ClientRegistrationRepository clientRegistrationRepository,
            SingleSignOnProperties properties, FluxHolder fluxHolder) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.properties = properties;
        this.fluxHolder = fluxHolder;
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

    /**
     * Returns the URL to call to perform a logout from the OAuth2 provider.
     *
     * @return the URL
     */
    public Optional<String> getLogoutUrl() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken
                && authentication.getPrincipal() instanceof OidcUser) {

            var logoutRedirectRoute = properties.getLogoutRedirectRoute();
            String logoutUri;

            // the logout redirect route can contain a {baseUrl} placeholder
            if (logoutRedirectRoute.contains("{baseUrl}")) {
                logoutUri = getCurrentHttpRequest()
                        .map(request -> UriComponentsBuilder
                                .fromHttpUrl(
                                        UrlUtils.buildFullRequestUrl(request))
                                .replacePath(request.getContextPath())
                                .replaceQuery(null).fragment(null).build()
                                .toUriString())
                        .map(uri -> logoutRedirectRoute.replace("{baseUrl}",
                                uri))
                        .orElse(logoutRedirectRoute);
            } else {
                logoutUri = logoutRedirectRoute;
            }

            // Build the logout URL according to the OpenID Connect
            // specification
            var ou = (OidcUser) authentication.getPrincipal();
            var registrationId = ((OAuth2AuthenticationToken) authentication)
                    .getAuthorizedClientRegistrationId();
            var clientRegistration = clientRegistrationRepository
                    .findByRegistrationId(registrationId);
            var details = clientRegistration.getProviderDetails();
            // The end_session_endpoint is buried in the provider metadata
            var endSessionEndpoint = details.getConfigurationMetadata()
                    .get("end_session_endpoint").toString();
            var builder = UriComponentsBuilder.fromUriString(endSessionEndpoint)
                    .queryParam("id_token_hint",
                            ou.getIdToken().getTokenValue())
                    .queryParam("post_logout_redirect_uri", logoutUri);
            return Optional.of(builder.toUriString());
        }

        return Optional.empty();
    }

    /**
     * Returns the ids of the registered OAuth2 clients.
     *
     * @return a list of registration ids
     */
    @Nonnull
    public List<@Nonnull String> getRegisteredClients() {
        return Optional.of(clientRegistrationRepository)
                // By default, the client registration repository is an instance
                // of InMemoryClientRegistrationRepository
                .filter(InMemoryClientRegistrationRepository.class::isInstance)
                .map(InMemoryClientRegistrationRepository.class::cast)
                .map(repo -> {
                    List<String> list = new ArrayList<>();
                    repo.iterator().forEachRemaining(registration -> list
                            .add(registration.getRegistrationId()));
                    return list;
                }).orElse(List.of());
    }

    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }

    @PermitAll
    @Nonnull
    public EndpointSubscription<@Nonnull String> backChannelLogout() {
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
