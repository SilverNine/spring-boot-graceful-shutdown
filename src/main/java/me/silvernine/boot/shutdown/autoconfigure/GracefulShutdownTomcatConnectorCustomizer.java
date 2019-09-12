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
    private final GracefulShutdownProperties gracefulShutdownProperties;

    private Connector connector;


    public GracefulShutdownTomcatConnectorCustomizer(ApplicationContext applicationContext, GracefulShutdownProperties gracefulShutdownProperties) {
        this.applicationContext = applicationContext;
        this.gracefulShutdownProperties = gracefulShutdownProperties;
    }


    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }


    @EventListener(ContextClosedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public void contextClosed(ContextClosedEvent contextClosedEvent) throws InterruptedException {
        if (connector == null) {
            return;
        }

        if (isEventFromLocalContext(contextClosedEvent)) {
            stopAcceptingNewRequests();
            shutdownThreadPoolExecutor();
        }
    }


    private void stopAcceptingNewRequests() {
        connector.pause();

        log.info("Paused {} to stop accepting new requests", connector);
    }


    private void shutdownThreadPoolExecutor() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();

        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
            awaitTermination(threadPoolExecutor);
        }
    }


    private void awaitTermination(ThreadPoolExecutor threadPoolExecutor) throws InterruptedException {
        for (long remaining = gracefulShutdownProperties.getTimeout().getSeconds(); remaining > 0; remaining -= CHECK_INTERVAL) {
            if (threadPoolExecutor.awaitTermination(CHECK_INTERVAL, TimeUnit.SECONDS)) {
                return;
            }

            log.info("{} thread(s) active, {} seconds remaining", threadPoolExecutor.getActiveCount(), remaining);
        }

        logMessageIfThereAreStillActiveThreads(threadPoolExecutor);
    }


    private void logMessageIfThereAreStillActiveThreads(ThreadPoolExecutor threadPoolExecutor) {
        if (threadPoolExecutor.getActiveCount() > 0) {
            log.warn("{} thread(s) still active, force shutdown", threadPoolExecutor.getActiveCount());
        }
    }


    private ThreadPoolExecutor getThreadPoolExecutor() {
        Executor executor = connector.getProtocolHandler().getExecutor();

        if (executor instanceof ThreadPoolExecutor) {
            return (ThreadPoolExecutor) executor;
        }

        return null;
    }


    private boolean isEventFromLocalContext(ContextClosedEvent contextClosedEvent) {
        return contextClosedEvent.getApplicationContext().equals(applicationContext);
    }
}
