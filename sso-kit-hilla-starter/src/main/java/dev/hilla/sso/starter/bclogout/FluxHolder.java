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

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class FluxHolder {

    private Consumer<Object> consumer;

    private final Flux<Object> flux;

    public FluxHolder() {
        flux = Flux.create(sink -> consumer = sink::next).share();
    }

    public Consumer<Object> getConsumer() {
        return consumer;
    }

    public Flux<Object> getFlux() {
        return flux;
    }
}
