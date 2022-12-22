package dev.hilla.sso.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@AutoConfiguration
@EnableWebSecurity
@ConditionalOnProperty(name = "auto-configure", prefix = SingleSignOnProperties.PREFIX, matchIfMissing = true)
@EnableConfigurationProperties(SingleSignOnProperties.class)
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
