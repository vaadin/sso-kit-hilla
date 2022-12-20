package dev.hilla.sso.endpoint;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import dev.hilla.Nonnull;

@Endpoint
@AnonymousAllowed
public class AuthEndpoint {

    @Nonnull
    public String getLoginURL() {
        return "/oauth2/authorization/keycloak";
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
                    return user;
                });
    }
}
