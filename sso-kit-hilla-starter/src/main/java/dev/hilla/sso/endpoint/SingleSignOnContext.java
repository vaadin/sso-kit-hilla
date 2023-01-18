package dev.hilla.sso.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import dev.hilla.Nonnull;
import dev.hilla.sso.starter.SingleSignOnProperties;
import dev.hilla.sso.starter.bclogout.BackChannelLogoutSubscription;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

@Component
public class SingleSignOnContext {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final SingleSignOnProperties properties;

    private final BackChannelLogoutSubscription backChannelLogoutSubscription;

    public SingleSignOnContext(
            ClientRegistrationRepository clientRegistrationRepository,
            SingleSignOnProperties properties,
            BackChannelLogoutSubscription backChannelLogoutSubscription) {
        Objects.requireNonNull(clientRegistrationRepository);
        Objects.requireNonNull(properties);
        Objects.requireNonNull(backChannelLogoutSubscription);
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.properties = properties;
        this.backChannelLogoutSubscription = backChannelLogoutSubscription;
    }

    public static Optional<OidcUser> getOidcUser() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(OidcUser.class::isInstance).map(OidcUser.class::cast);
    }

    static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }

    public List<@Nonnull String> getRegisteredProviders() {
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

    public boolean isBackChannelLogoutEnabled() {
        return properties.isBackChannelLogout();
    }

    public Optional<String> getLogoutUrl(OidcUser user) {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .map(OAuth2AuthenticationToken.class::cast)
                .map(token -> buildLogoutUrl(user, token));
    }

    private String buildLogoutUrl(OidcUser user,
            OAuth2AuthenticationToken authenticationToken) {
        // Build the logout URL according to the OpenID Connect specification
        var registrationId = authenticationToken
                .getAuthorizedClientRegistrationId();
        var clientRegistration = clientRegistrationRepository
                .findByRegistrationId(registrationId);
        var details = clientRegistration.getProviderDetails();
        // The end_session_endpoint is buried in the provider metadata
        var endSessionEndpoint = details.getConfigurationMetadata()
                .get("end_session_endpoint").toString();
        var builder = UriComponentsBuilder.fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue())
                .queryParam("post_logout_redirect_uri",
                        getPostLogoutRedirectUri());
        return builder.toUriString();
    }

    private String getPostLogoutRedirectUri() {
        var logoutRedirectRoute = properties.getLogoutRedirectRoute();
        String logoutUri;

        // the logout redirect route can contain a {baseUrl} placeholder
        if (logoutRedirectRoute.contains("{baseUrl}")) {
            logoutUri = getCurrentHttpRequest()
                    .map(request -> UriComponentsBuilder
                            .fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                            .replacePath(request.getContextPath())
                            .replaceQuery(null).fragment(null).build()
                            .toUriString())
                    .map(uri -> logoutRedirectRoute.replace("{baseUrl}", uri))
                    .orElse(logoutRedirectRoute);
        } else {
            logoutUri = logoutRedirectRoute;
        }
        return logoutUri;
    }

    public Flux<String> getStringFlux() {
        var principal = SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return backChannelLogoutSubscription.getFluxForUser(principal);
    }

    public SingleSignOnData getSingleSignOnData() {
        SingleSignOnData data = new SingleSignOnData();
        data.setRegisteredProviders(getRegisteredProviders());

        getOidcUser().ifPresent(u -> {
            data.setUser(User.from(u));
            data.setLogoutUrl(getLogoutUrl(u).orElseThrow());
            data.setBackChannelLogoutEnabled(isBackChannelLogoutEnabled());
        });

        return data;
    }
}
