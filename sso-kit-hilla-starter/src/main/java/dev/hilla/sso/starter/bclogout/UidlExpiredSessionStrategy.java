/*-
 * Copyright (C) 2022 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package dev.hilla.sso.starter.bclogout;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import com.vaadin.flow.server.HandlerHelper;

/**
 * A strategy to handle expired sessions which is aware of UIDL requests.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public class UidlExpiredSessionStrategy
        implements SessionInformationExpiredStrategy {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UidlExpiredSessionStrategy.class);

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException, ServletException {
        final var request = event.getRequest();
        final var response = event.getResponse();
        final var redirectRoute = '/' + request.getContextPath();
        final var servletMapping = request.getHttpServletMapping().getPattern();
        if (HandlerHelper.isFrameworkInternalRequest(servletMapping, request)) {
            LOGGER.debug("Session expired during internal request: writing "
                    + "a Vaadin-Refresh token to the response body");
            response.getWriter().write("Vaadin-Refresh: " + redirectRoute);
        } else {
            LOGGER.debug("Session expired: redirecting to " + redirectRoute);
            response.setStatus(302);
            response.setHeader("Location", redirectRoute);
        }
    }
}
