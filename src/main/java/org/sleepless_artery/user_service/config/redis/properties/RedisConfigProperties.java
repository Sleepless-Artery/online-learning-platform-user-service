package org.sleepless_artery.user_service.config.redis.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Getter @Setter
public class RedisConfigProperties {

    private String host;
    private int port;
    private String password;

    private Cluster cluster = new Cluster();


    @Getter @Setter
    public static class Cluster {
        private String nodes;
        private Integer maxRedirects;
    }

    public boolean isClusterMode() {
        return cluster != null && cluster.getNodes() != null && !cluster.getNodes().isEmpty();
    }
}
