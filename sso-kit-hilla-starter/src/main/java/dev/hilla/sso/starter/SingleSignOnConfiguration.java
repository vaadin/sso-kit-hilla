/*-
 * Copyright (C) 2022-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package dev.hilla.sso.starter;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import dev.hilla.sso.starter.bclogout.BackChannelLogoutFilter;
import dev.hilla.sso.starter.bclogout.FluxHolder;
import dev.hilla.sso.starter.bclogout.UidlExpiredSessionStrategy;

@Configuration
@EnableWebSecurity
public class SingleSignOnConfiguration extends VaadinWebSecurity {

    private final SingleSignOnProperties properties;

    private final SingleSignOnUserService userService;

    private final BackChannelLogoutFilter backChannelLogoutFilter;

    private final SessionRegistry sessionRegistry;

    public SingleSignOnConfiguration(SingleSignOnProperties properties,
            SessionRegistry sessionRegistry,
            ClientRegistrationRepository clientRegistrationRepository,
            FluxHolder fluxHolder) {
        this.properties = properties;
        this.sessionRegistry = sessionRegistry;
        userService = new SingleSignOnUserService();
        backChannelLogoutFilter = new BackChannelLogoutFilter(sessionRegistry,
                clientRegistrationRepository, fluxHolder);
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2Login().userInfoEndpoint().oidcUserService(userService).and()
                .loginPage(properties.getLoginRoute()).and().logout()
                .logoutSuccessUrl("/");

        if (properties.isBackChannelLogout()) {
            var backChannelLogoutRoute = Objects.requireNonNullElse(
                    properties.getBackChannelLogoutRoute(),
                    SingleSignOnProperties.DEFAULT_BACKCHANNEL_LOGOUT_ROUTE);
            backChannelLogoutFilter
                    .setBackChannelLogoutRoute(backChannelLogoutRoute);

            // Adds the Back-Channel logout filter to the filter chain
            http.addFilterAfter(backChannelLogoutFilter, LogoutFilter.class);

            // Disable CSRF for Back-Channel logout requests
            final var matcher = backChannelLogoutFilter.getRequestMatcher();
            http.csrf().ignoringRequestMatchers(matcher);

            var maximumSessions = properties.getMaximumConcurrentSessions();
            http.sessionManagement()
                    .sessionConcurrency(sessionConcurrencyCustomizer -> {
                        sessionConcurrencyCustomizer
                                .maximumSessions(maximumSessions);
                        sessionConcurrencyCustomizer
                                .sessionRegistry(sessionRegistry);
                        var expiredStrategy = new UidlExpiredSessionStrategy();
                        sessionConcurrencyCustomizer
                                .expiredSessionStrategy(expiredStrategy);
                    });
        }

        return http.build();
    }
}
