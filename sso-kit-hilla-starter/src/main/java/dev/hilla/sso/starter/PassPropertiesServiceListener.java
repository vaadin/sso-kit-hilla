package dev.hilla.sso.starter;

import org.springframework.stereotype.Component;

import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.VaadinServiceInitListener;

import dev.hilla.sso.endpoint.ClientParameters;
import elemental.json.JsonObject;

@Component
public class PassPropertiesServiceListener
        implements VaadinServiceInitListener {

    private final ClientParameters clientParameters;

    public PassPropertiesServiceListener(ClientParameters clientParameters) {
        this.clientParameters = clientParameters;
    }

    @Override
    public void serviceInit(com.vaadin.flow.server.ServiceInitEvent event) {
        JsonObject jsonObject = JsonUtils.beanToJson(clientParameters);

        event.addIndexHtmlRequestListener(indexHtmlResponse -> {
            indexHtmlResponse.getDocument().body().appendChild(indexHtmlResponse
                    .getDocument().createElement("script")
                    .text("window.Hilla = window.Hilla || {};\nHilla.SSO = "
                            + jsonObject.toJson() + ";"));
        });
    }
}
