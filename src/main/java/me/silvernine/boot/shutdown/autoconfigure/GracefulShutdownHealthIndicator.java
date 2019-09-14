package me.silvernine.boot.shutdown.autoconfigure;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;


@Slf4j
public class GracefulShutdownHealthIndicator implements HealthIndicator {

    private final ApplicationContext applicationContext;
    private final GracefulShutdownProperties gracefulShutdownProperties;

    private Health health = Health.up().build();


    public GracefulShutdownHealthIndicator(ApplicationContext applicationContext, GracefulShutdownProperties gracefulShutdownProperties) {
        this.applicationContext = applicationContext;
        this.gracefulShutdownProperties = gracefulShutdownProperties;
    }


    @Override
    public Health health() {
        return health;
    }


    @EventListener(ContextClosedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void contextClosed(ContextClosedEvent contextClosedEvent) throws InterruptedException {
        if (isEventFromLocalContext(contextClosedEvent)) {
            updateHealthToOutOfService();
            waitForKubernetesToSeeOutOfService();
        }
    }


    private void updateHealthToOutOfService() {
        health = Health.outOfService().build();

        log.info("Health status set to out of service");
    }


    private void waitForKubernetesToSeeOutOfService() throws InterruptedException {
        log.info("Wait {} seconds for Kubernetes to see the out of service status", gracefulShutdownProperties.getWait().getSeconds());

        Thread.sleep(gracefulShutdownProperties.getWait().toMillis());
    }


    private boolean isEventFromLocalContext(ContextClosedEvent contextClosedEvent) {
        return contextClosedEvent.getApplicationContext().equals(applicationContext);
    }
}
