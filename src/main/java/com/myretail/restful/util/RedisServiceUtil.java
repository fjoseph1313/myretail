package com.myretail.restful.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisServiceUtil {
    /**
     * This is just a simple hack to let us use embedded Redis standalone cluster without "boostrap" error.
     * since Redis server is initialized after beans creation
     */
    private LettuceConnectionFactory lettuceConnectionFactory;
    private RedisTemplate<String, Object> redisTemplate;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }

    public LettuceConnectionFactory lettuceConnectionFactory() {
        if (Objects.isNull(lettuceConnectionFactory)) {
            lettuceConnectionFactory = new LettuceConnectionFactory("localhost", 6379);
            lettuceConnectionFactory.afterPropertiesSet();
        }
        return lettuceConnectionFactory;
    }

    public RedisTemplate<String, Object> getRedisInstance() {
        if (Objects.isNull(redisTemplate)) {
            redisTemplate = redisTemplate();
        }
        return redisTemplate;
    }

    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        template.afterPropertiesSet();
        return template;
    }

    public String getValue(final String key) {
        try {
            return (String) this.getRedisInstance().opsForValue().get(key);
        } catch (Exception ex) {
            log.error("Redis Server/connection was down: {}", ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     * The TimeToLive could be configurable from properties file
     * Keys will expire in 1 minutes, in this case.
     * IMPROVEMENT: DO NOT HALT THE PROCESS WHEN REDIS ISN'T ONLINE
     *
     * @param key   -
     * @param value -
     */
    public void setValue(final String key, final String value) {
        try {
            this.getRedisInstance().opsForValue().set(key, value);
            this.getRedisInstance().expire(key, 1, TimeUnit.MINUTES);
        } catch (Exception ex) {
            log.error("Redis Server/connection was down: {}", ex.getLocalizedMessage());
            //Do Nothing.
        }
    }

    public String getProductKey(Long productId) {
        return String.format("product:%s", productId);
    }
}
