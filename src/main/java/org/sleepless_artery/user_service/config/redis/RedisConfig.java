package org.sleepless_artery.user_service.config.redis;

import io.lettuce.core.ClientOptions;
import lombok.RequiredArgsConstructor;
import org.sleepless_artery.user_service.config.redis.properties.RedisConfigProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisConfigProperties redisConfigProperties;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            String password = redisConfigProperties.getPassword();

            if (redisConfigProperties.isClusterMode()) {
                return createClusterConnectionFactory(password);
            } else {
                return createStandaloneConnectionFactory(password);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Redis connection factory", e);
        }
    }


    private RedisConnectionFactory createStandaloneConnectionFactory(String password) {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(
                redisConfigProperties.getHost(), redisConfigProperties.getPort()
        );

        if (password != null && !password.isBlank()) {
            standaloneConfiguration.setPassword(RedisPassword.of(password));
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfiguration);
        factory.afterPropertiesSet();
        return factory;
    }


    private RedisConnectionFactory createClusterConnectionFactory(String password) {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

        Arrays.stream(
                redisConfigProperties.getCluster().getNodes().split(",")
        ).forEach(node -> {
            String[] parts = node.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cluster node format: " + node);
            }
            clusterConfiguration.clusterNode(parts[0], Integer.parseInt(parts[1]));
        });

        if (password != null && !password.isBlank()) {
            clusterConfiguration.setPassword(RedisPassword.of(password));
        }

        if (redisConfigProperties.getCluster().getMaxRedirects() != null) {
            clusterConfiguration.setMaxRedirects(redisConfigProperties.getCluster().getMaxRedirects());
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2))
                .clientOptions(ClientOptions.builder()
                        .autoReconnect(true)
                        .build())
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(clusterConfiguration, clientConfig);
        factory.afterPropertiesSet();
        return factory;
    }
    

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager() {
        Map<String, RedisCacheConfiguration> redisCacheConfiguration = new HashMap<>();

        redisCacheConfiguration.put("users-by-id",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofHours(12))
                        .disableCachingNullValues()
        );

        redisCacheConfiguration.put("users-by-email",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofHours(12))
                        .disableCachingNullValues()
        );


        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofHours(1)))
                .withInitialCacheConfigurations(redisCacheConfiguration)
                .build();
    }
}
