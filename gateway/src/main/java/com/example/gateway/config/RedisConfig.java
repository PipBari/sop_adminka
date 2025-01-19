package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    public static final String REDIS_ALL_RECIPES_CACHE_KEY = "allRecipes";
    public static final String REDIS_RECIPE_BY_ID_CACHE_KEY = "recipeById";

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        if (redisHost == null || redisHost.isBlank() || redisPort <= 0) {
            throw new IllegalStateException("Redis configuration is invalid. Check 'spring.data.redis.host' and 'spring.data.redis.port'.");
        }
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = defaultCacheConfig(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration(REDIS_ALL_RECIPES_CACHE_KEY, defaultCacheConfig(Duration.ofMinutes(10)))
                .withCacheConfiguration(REDIS_RECIPE_BY_ID_CACHE_KEY, defaultCacheConfig(Duration.ofMinutes(10)))
                .build();
    }

    private RedisCacheConfiguration defaultCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
