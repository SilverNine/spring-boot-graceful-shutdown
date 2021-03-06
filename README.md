Spring Boot Graceful Shutdown
=============================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.silvernine/spring-boot-graceful-shutdown/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.silvernine/spring-boot-graceful-shutdown)

This project adds graceful shutdown behavior to Spring Boot.

The project referred to "https://github.com/timpeeters/spring-boot-graceful-shutdown" and tried to improve it further.


Versions
--------

Multiple branches are maintained to support multiple Spring Boot versions.
The following tables show the relation between the Spring Boot version and the Spring Boot Actuator Server Config version.

| Spring Boot | Spring Boot Actuator Server Config | Branch |
| :---        | :---                               | :---   |
| 2.1.x       | 2.1.x                              | master |


Flow
----

1. The JVM receives the SIGTERM signal and starts shutting down the Spring container.
2. A Spring EventListener listens for a ContextClosedEvent and is invoked once the shutdown is started.
3. The EventListener updates a Spring Boot HealthIndicator and puts it "out of service".
5. The context shutdown is delayed using a Thread.sleep to allow the load balancer to see the updated HealthIndicator status and stop forwarding requests to this instance.
7. When the Thread.sleep is finished, the Tomcat container is gracefully shutdown. 
First by pausing the connector, no longer accepting new request.
Next, by allowing the Tomcat thread pool a configurable amount of time to finish the active threads.
8. Finally, the Spring context is closed.


Limitations
-----------

1. Currently this project only supports Tomcat as embedded web container for Spring Boot. ( Undertow and/or Jetty are not yet supported. )
2. org.springframework.boot:spring-boot-starter-actuator is required


Installation
------------

for Maven:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <version>X.X.X.RELEASE</version>
</dependency>
<dependency>
    <groupId>me.silvernine</groupId>
    <artifactId>spring-graceful-shutdown</artifactId>
    <version>X.X.X</version>
</dependency>
```

for Gradle:

```groovy
compile 'org.springframework.boot:spring-boot-starter-actuator:X.X.X.RELEASE'
compile 'me.silvernine:spring-boot-graceful-shutdown:X.X.X'
```


Configuration
-------------

| Key                       | Default value | Description |
| ------------------------- | ------------- | ----------- |
| graceful.shutdown.enabled | false         | Indicates whether graceful shutdown is enabled or not. | 
| graceful.shutdown.timeout | 60s           | The number of seconds to wait for active threads to finish before shutting down the Tomcat connector. |
| graceful.shutdown.wait    | 30s           | The number of seconds to return "out of service" on the health page before starting the graceful shutdown. |


Alternative implementations
---------------------------

We found several alternatives for graceful shutdown behavior in Spring Boot.
 
- https://github.com/SchweizerischeBundesbahnen/springboot-graceful-shutdown
- https://github.com/corentin59/spring-boot-graceful-shutdown
- https://github.com/gesellix/graceful-shutdown-spring-boot


References
----------

- https://github.com/spring-projects/spring-boot/issues/4657
