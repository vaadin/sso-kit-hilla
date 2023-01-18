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

import org.springframework.context.ApplicationEventPublisher;
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
import dev.hilla.sso.starter.bclogout.BackChannelLogoutSubscription;

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
            ApplicationEventPublisher eventPublisher) {
        this.properties = properties;
        this.sessionRegistry = sessionRegistry;
        userService = new SingleSignOnUserService();
        backChannelLogoutFilter = new BackChannelLogoutFilter(sessionRegistry,
                clientRegistrationRepository, eventPublisher);
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2Login().userInfoEndpoint().oidcUserService(userService).and()
                .loginPage(properties.getLoginRoute()).and().logout()
                .logoutSuccessUrl("/").and().sessionManagement()
                .sessionConcurrency(concurrency -> {
                    // Sets the maximum number of concurrent sessions per user.
                    // The default is -1 which means no limit on the number of
                    // concurrent sessions per user.
                    concurrency.maximumSessions(
                            properties.getMaximumConcurrentSessions());
                    // Sets the session-registry which is used for Back-Channel
                    concurrency.sessionRegistry(sessionRegistry);
                });

        if (properties.isBackChannelLogout()) {
            backChannelLogoutFilter.setBackChannelLogoutRoute(
                    properties.getBackChannelLogoutRoute());

            // Adds the Back-Channel logout filter to the filter chain
            http.addFilterAfter(backChannelLogoutFilter, LogoutFilter.class);

            // Disable CSRF for Back-Channel logout requests
            final var matcher = backChannelLogoutFilter.getRequestMatcher();
            http.csrf().ignoringRequestMatchers(matcher);
        }

        return http.build();
    }
}
