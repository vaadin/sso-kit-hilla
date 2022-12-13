package com.example.application;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.vaadin.sso.starter.SingleSignOnConfiguration;
import com.vaadin.sso.starter.SingleSignOnProperties;

@AutoConfiguration
@EnableWebSecurity
@Conditional(ClientsConfiguredCondition.class)
@ConditionalOnProperty(name = "auto-configure", prefix = "hilla.sso", matchIfMissing = true)
public class SSOConfig extends SingleSignOnConfiguration {

    public SSOConfig(SingleSignOnProperties properties,
            SessionRegistry sessionRegistry,
            ClientRegistrationRepository clientRegistrationRepository) {
        super(properties, sessionRegistry, clientRegistrationRepository);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/**").permitAll();
        super.configure(http);
    }
}
