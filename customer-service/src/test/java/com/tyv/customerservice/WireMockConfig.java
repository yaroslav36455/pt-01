package com.tyv.customerservice;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class WireMockConfig implements ApplicationContextAware {
    public static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        WireMockConfig.context = context;
    }

    public static int getPort() {
        return Integer.parseInt(context.getEnvironment().getProperty("wiremock.server.port", "8080"));
    }

    public static String getBaseUrl() {
        return "http://localhost:" + WireMockConfig.getPort();
    }
}
