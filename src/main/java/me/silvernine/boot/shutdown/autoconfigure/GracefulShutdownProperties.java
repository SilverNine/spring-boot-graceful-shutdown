package me.silvernine.boot.shutdown.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.StringJoiner;

@ConfigurationProperties("graceful.shutdown")
@Slf4j
public class GracefulShutdownProperties implements InitializingBean {

    /**
     * Indicates whether graceful shutdown is enabled or not.
     */
    private boolean enabled;

    /**
     * The number of seconds to wait for active threads to finish before shutting down the embedded web container.
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * The number of seconds to wait before starting the graceful shutdown. During this time, the health checker returns
     * OUT_OF_SERVICE.
     */
    private Duration wait = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getWait() {
        return wait;
    }

    public void setWait(Duration wait) {
        this.wait = wait;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "GracefulShutdownProperties[", "]")
                .add("enabled=" + isEnabled())
                .add("timeout=" + getTimeout())
                .add("wait=" + getWait())
                .toString();
    }

    @Override
    public void afterPropertiesSet() {
        log.info(toString());
    }
}
