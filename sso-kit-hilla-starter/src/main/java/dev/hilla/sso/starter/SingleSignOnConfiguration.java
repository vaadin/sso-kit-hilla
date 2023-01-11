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

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
@EnableWebSecurity
public class SingleSignOnConfiguration extends VaadinWebSecurity {

    private final SingleSignOnProperties properties;

    private final SingleSignOnUserService userService;

    public SingleSignOnConfiguration(SingleSignOnProperties properties) {
        this.properties = properties;
        userService = new SingleSignOnUserService();
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2Login().userInfoEndpoint().oidcUserService(userService).and()
                .loginPage(properties.getLoginRoute()).and().logout()
                .logoutSuccessUrl("/");
        return http.build();
    }
}
