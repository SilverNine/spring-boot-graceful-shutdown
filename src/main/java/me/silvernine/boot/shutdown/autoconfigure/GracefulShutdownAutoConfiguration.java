package me.silvernine.boot.shutdown.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(havingValue = "true", prefix = "graceful.shutdown", name = "enabled")
@ConditionalOnWebApplication
@Configuration
@Slf4j
@EnableConfigurationProperties(GracefulShutdownProperties.class)
public class GracefulShutdownAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator(
            ApplicationContext applicationContext, GracefulShutdownProperties gracefulShutdownProperties) {

        return new GracefulShutdownHealthIndicator(applicationContext, gracefulShutdownProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GracefulShutdownTomcatContainerCustomizer gracefulShutdownTomcatContainerCustomizer(
            GracefulShutdownTomcatConnectorCustomizer gracefulShutdownTomcatConnectorCustomizer) {

        return new GracefulShutdownTomcatContainerCustomizer(gracefulShutdownTomcatConnectorCustomizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public GracefulShutdownTomcatConnectorCustomizer gracefulShutdownTomcatConnectorCustomizer(
            ApplicationContext applicationContext, GracefulShutdownProperties gracefulShutdownProperties) {

        return new GracefulShutdownTomcatConnectorCustomizer(applicationContext, gracefulShutdownProperties);
    }
}
