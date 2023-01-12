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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringSecurityAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test suite for {@link SingleSignOnConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
public class SingleSignOnConfigurationTest {

    private WebApplicationContextRunner contextRunner;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    public void init() {
        contextRunner = new WebApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(SpringBootAutoConfiguration.class,
                                SpringSecurityAutoConfiguration.class,
                                SingleSignOnConfiguration.class,
                                SingleSignOnProperties.class,
                                SingleSignOnDefaultBeans.class))
                .withPropertyValues(
                        "spring.security.oauth2.client.registration.foo.client-id=foo")
                .withBean(ClientRegistrationRepository.class,
                        () -> clientRegistrationRepository);
    }

    @Test
    public void autoConfigureProperty_notSet_configurationEnabled() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(SingleSignOnConfiguration.class);
        });
    }

    @Test
    public void loginRouteProperty_hasDefaultValue() {
        contextRunner.run(ctx -> {
            var loginRoute = ctx.getBean(SingleSignOnProperties.class)
                    .getLoginRoute();
            assertEquals(SingleSignOnProperties.DEFAULT_LOGIN_ROUTE,
                    loginRoute);
        });
    }

    @Test
    public void loginRouteProperty_hasCustomValue() {
        contextRunner.withPropertyValues("hilla.sso.login-route=/custom")
                .run(ctx -> {
                    var loginRoute = ctx.getBean(SingleSignOnProperties.class)
                            .getLoginRoute();
                    assertEquals("/custom", loginRoute);
                });
    }
}
