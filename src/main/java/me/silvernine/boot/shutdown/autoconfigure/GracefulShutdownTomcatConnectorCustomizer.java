package me.silvernine.boot.shutdown.autoconfigure;


import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class GracefulShutdownTomcatConnectorCustomizer implements TomcatConnectorCustomizer {

    private static final int CHECK_INTERVAL = 10;

    private final ApplicationContext applicationContext;
    private final GracefulShutdownProperties props;

    private Connector connector;


    public GracefulShutdownTomcatConnectorCustomizer(ApplicationContext ctx, GracefulShutdownProperties props) {
        this.applicationContext = ctx;
        this.props = props;
    }


    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }


    @EventListener(ContextClosedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void contextClosed(ContextClosedEvent event) throws InterruptedException {
        if (connector == null) {
            return;
        }

        if (isEventFromLocalContext(event)) {
            stopAcceptingNewRequests();
            shutdownThreadPoolExecutor();
        }
    }


    private void stopAcceptingNewRequests() {
        connector.pause();

        log.info("Paused {} to stop accepting new requests", connector);
    }


    private void shutdownThreadPoolExecutor() throws InterruptedException {
        ThreadPoolExecutor executor = getThreadPoolExecutor();

        if (executor != null) {
            executor.shutdown();
            awaitTermination(executor);
        }
    }


    private void awaitTermination(ThreadPoolExecutor executor) throws InterruptedException {
        for (long remaining = props.getTimeout().getSeconds(); remaining > 0; remaining -= CHECK_INTERVAL) {
            if (executor.awaitTermination(CHECK_INTERVAL, TimeUnit.SECONDS)) {
                return;
            }

            log.info("{} thread(s) active, {} seconds remaining", executor.getActiveCount(), remaining);
        }

        logMessageIfThereAreStillActiveThreads(executor);
    }


    private void logMessageIfThereAreStillActiveThreads(ThreadPoolExecutor executor) {
        if (executor.getActiveCount() > 0) {
            log.warn("{} thread(s) still active, force shutdown", executor.getActiveCount());
        }
    }


    private ThreadPoolExecutor getThreadPoolExecutor() {
        Executor executor = connector.getProtocolHandler().getExecutor();

        if (executor instanceof ThreadPoolExecutor) {
            return (ThreadPoolExecutor) executor;
        }

        return null;
    }


    private boolean isEventFromLocalContext(ContextClosedEvent event) {
        return event.getApplicationContext().equals(applicationContext);
    }
}
