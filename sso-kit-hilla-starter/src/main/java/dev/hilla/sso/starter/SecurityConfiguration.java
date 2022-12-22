package dev.hilla.sso.starter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    public static final String LOGOUT_URL = "/logout";

    private final KeycloakLogoutHandler keycloakLogoutHandler;

    public SecurityConfiguration(KeycloakLogoutHandler keycloakLogoutHandler) {
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    // The secret is stored in /config/secrets/application.properties by
    // default.
    // Never commit the secret into version control; each environment should
    // have
    // its own secret.
    // @Value("${com.example.application.auth.secret}")
    // private String authSecret;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/images/*.png"))
                .permitAll();
        super.configure(http);

        // http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // setLoginView(http, "/login", LOGOUT_URL);
        // setStatelessAuthentication(http, new
        // SecretKeySpec(Base64.getDecoder().decode(authSecret),
        // JwsAlgorithms.HS256),
        // "com.example.application");
        // http.oauth2Login(o -> {
        // o.loginPage("/oauth2/authorization/keycloak");
        // }).logout(l -> {
        // l.logoutUrl("/logout");
        // });
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2Login().loginPage("/oauth2/authorization/keycloak").and()
                .logout().addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl("/");
        return http.build();
    }
}
