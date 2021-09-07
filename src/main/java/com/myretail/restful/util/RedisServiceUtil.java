package com.myretail.restful.util;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
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

@Component
public class RedisServiceUtil {
    /**
     * This is just a simple hack to let us use embedded Redis standalone cluster without "boostrap" error.
     * since Redis server is initialized after beans creation
     */
    private JedisConnectionFactory jedisConnectionFactory;
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

    public JedisConnectionFactory jedisConnectionFactory() {
        if (Objects.isNull(jedisConnectionFactory)) {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("localhost", 6379);
            JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
            jedisConnectionFactory = new JedisConnectionFactory(configuration,jedisClientConfiguration);
            jedisConnectionFactory.afterPropertiesSet();
        }
        return jedisConnectionFactory;
    }

    public RedisTemplate<String, Object> getRedisInstance() {
        if (Objects.isNull(redisTemplate)) {
            redisTemplate = redisTemplate();
        }
        return redisTemplate;
    }

    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        template.afterPropertiesSet();
        return template;
    }

    public String getValue(final String key) {
        return (String) this.getRedisInstance().opsForValue().get(key);
    }

    /**
     * The TimeToLive could be configurable from properties file
     * Keys will expire in 1 minutes, in this case.
     * @param key -
     * @param value -
     */
    public void setValue(final String key, final String value) {
        this.getRedisInstance().opsForValue().set(key, value);
        this.getRedisInstance().expire(key, 1, TimeUnit.MINUTES);
    }

    public String getProductKey(Long productId) {
        return String.format("product:%s", productId);
    }
}
