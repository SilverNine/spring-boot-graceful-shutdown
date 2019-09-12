package me.silvernine.boot.shutdown.autoconfigure;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class GracefulShutdownTomcatContainerCustomizer
        implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final GracefulShutdownTomcatConnectorCustomizer gracefulShutdownTomcatConnectorCustomizer;

    public GracefulShutdownTomcatContainerCustomizer(GracefulShutdownTomcatConnectorCustomizer gracefulShutdownTomcatConnectorCustomizer) {
        this.gracefulShutdownTomcatConnectorCustomizer = gracefulShutdownTomcatConnectorCustomizer;
    }

    @Override
    public void customize(TomcatServletWebServerFactory tomcatServletWebServerFactory) {
        tomcatServletWebServerFactory.addConnectorCustomizers(gracefulShutdownTomcatConnectorCustomizer);
    }
}
