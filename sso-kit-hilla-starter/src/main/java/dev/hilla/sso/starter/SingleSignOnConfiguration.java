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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
@EnableWebSecurity
public class SingleSignOnConfiguration extends VaadinWebSecurity {

    private final SingleSignOnProperties properties;

    private final KeycloakLogoutHandler keycloakLogoutHandler;

    public SingleSignOnConfiguration(SingleSignOnProperties properties,
            KeycloakLogoutHandler keycloakLogoutHandler) {
        this.properties = properties;
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/images/*.png"))
                .permitAll();
        super.configure(http);
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2Login().loginPage(properties.getLoginRoute()).and().logout()
                .addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl(properties.getLogoutRedirectRoute());
        return http.build();
    }
}
