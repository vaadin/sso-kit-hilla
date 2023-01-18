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

import org.springframework.context.ApplicationEvent;

/**
 * An event signalling that a user (principal) has been logged out.
 */
public class UserLogoutEvent extends ApplicationEvent {

    public UserLogoutEvent(Object principal) {
        super(principal);
    }
}
