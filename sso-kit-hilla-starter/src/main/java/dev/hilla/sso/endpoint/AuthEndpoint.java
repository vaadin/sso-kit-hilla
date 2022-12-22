package dev.hilla.sso.endpoint;

import java.util.Optional;
import java.util.stream.Collectors;

import dev.hilla.Nonnull;
import dev.hilla.sso.starter.SingleSignOnProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;

@Endpoint
@AnonymousAllowed
public class AuthEndpoint {

    private final SingleSignOnProperties properties;

    public AuthEndpoint(SingleSignOnProperties properties) {
        this.properties = properties;
    }

    @Nonnull
    public String getLoginRoute() {
        return properties.getLoginRoute();
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
                            .collect(Collectors.toSet()));
                    return user;
                });
    }
}
