/*-
 * Copyright (C) 2022-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package dev.hilla.sso.starter.bclogout;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class BackChannelLogoutSubscription
        implements ApplicationListener<UserLogoutEvent> {

    private Consumer<Object> consumer;

    private final Flux<Object> flux;

    public BackChannelLogoutSubscription() {
        flux = Flux.create(sink -> consumer = sink::next).share();
    }

    @Override
    public void onApplicationEvent(UserLogoutEvent event) {
        broadcast(event.getSource());
    }

    public Flux<String> getFluxForUser(Object principal) {
        Objects.requireNonNull(principal);
        return flux.filter(p -> Objects.equals(p, principal))
                .map(p -> "Your session has been terminated");
    }

    public boolean broadcast(Object principal) {
        if (consumer != null) {
            consumer.accept(principal);
            return true;
        }

        return false;
    }
}
