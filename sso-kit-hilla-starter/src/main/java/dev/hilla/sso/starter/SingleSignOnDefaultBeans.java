/*-
 * Copyright (C) 2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package dev.hilla.sso.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import dev.hilla.sso.starter.bclogout.FluxHolder;

/**
 * This configuration class provides default instances for the required beans.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
@AutoConfiguration
public class SingleSignOnDefaultBeans {

    /**
     * Provides a default {@link SessionRegistry} bean.
     *
     * @return the session registry bean
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionRegistry getSessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FluxHolder getFluxSinkHolder() {
        return new FluxHolder();
    }

    // @Bean
    // @ConditionalOnMissingBean
    // public ClientRegistrationRepository getClientRegistrationRepository() {
    // return new InMemoryClientRegistrationRepository();
    // }
}
