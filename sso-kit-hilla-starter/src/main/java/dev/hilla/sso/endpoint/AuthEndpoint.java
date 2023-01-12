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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

import java.util.Objects;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.hilla.EndpointSubscription;
import dev.hilla.sso.starter.bclogout.FluxHolder;

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

    @Nonnull
    public AuthInfo getAuthInfo() {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setRegisteredProviders(getRegisteredProviders());

        getOidcUser().ifPresent(u -> {
            authInfo.setUser(Optional.of(getAuthenticatedUser(u)));
            authInfo.setLogoutUrl(Optional.ofNullable(getLogoutUrl(u)));
            authInfo.setBackChannelLogoutEnabled(
                    properties.isBackChannelLogout());
        });

        return authInfo;
    }

    @PermitAll
    @Nonnull
    public EndpointSubscription<@Nonnull String> backChannelLogout() {
        LOGGER.debug("Client subscribed to back channel logout information");
        var principal = SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        var flux = fluxHolder.getFlux()
                .filter(p -> Objects.equals(p, principal))
                .map(p -> "Your session has been terminated");

        return EndpointSubscription.of(flux, () -> {
            LOGGER.debug(
                    "Client cancelled subscription to back channel logout information");
        });
    }

    @Nonnull
    private List<@Nonnull String> getRegisteredProviders() {
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

    @Nonnull
    private User getAuthenticatedUser(OidcUser ou) {
        User user = new User();
        user.setBirthdate(ou.getBirthdate());
        user.setEmail(ou.getEmail());
        user.setFamilyName(ou.getFamilyName());
        user.setFullName(ou.getFullName());
        user.setGender(ou.getGender());
        user.setGivenName(ou.getGivenName());
        user.setLocale(ou.getLocale());
        user.setMiddleName(ou.getMiddleName());
        user.setNickName(ou.getNickName());
        user.setPhoneNumber(ou.getPhoneNumber());
        user.setPicture(ou.getPicture());
        user.setPreferredUsername(ou.getPreferredUsername());

        user.setRoles(
                ou.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .filter(a -> a.startsWith(ROLE_PREFIX))
                        .map(a -> a.substring(ROLE_PREFIX_LENGTH))
                        .collect(Collectors.toSet()));
        return user;
    }

    private String getLogoutUrl(OidcUser ou) {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken) {
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
            return builder.toUriString();
        }

        return null;
    }

    public static Optional<OidcUser> getOidcUser() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(OidcUser.class::isInstance).map(OidcUser.class::cast);
    }

    private static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }
}
