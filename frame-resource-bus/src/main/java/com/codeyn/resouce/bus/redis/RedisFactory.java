package com.codeyn.resouce.bus.redis;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import redis.clients.jedis.JedisPoolConfig;

import com.codeyn.resouce.bus.ConfigLoader;

public class RedisFactory {

    private static final Logger logger = LoggerFactory.getLogger(RedisFactory.class);

    private static ConcurrentHashMap<String, RedisConnectionFactory> factoryCache = new ConcurrentHashMap<>();

    public static RedisConnectionFactory getConnectionFactory(String key) {
        return getConnectionFactory(key, new JedisPoolConfig());
    }

    public static RedisConnectionFactory getConnectionFactory(String key, JedisPoolConfig jedisPoolConfig) {
        Assert.notNull(key, "'key' is required; it must not be null!");
        RedisConnectionFactory currentFactory = factoryCache.get(key);
        if (currentFactory == null) {
            synchronized (RedisFactory.class) {
                currentFactory = factoryCache.get(key);
                if (currentFactory == null) {
                    RedisConfig config = ConfigLoader.getJedisFactoryConfig(key);
                    if (config == null) {
                        throw new IllegalStateException("Cannot find factoryConfig. key=" + key);
                    }
                    RedisConnectionFactory factory = getConnectionFactory(config, jedisPoolConfig);
                    RedisConnectionFactory exist = factoryCache.putIfAbsent(key, factory);
                    if (exist == null) {
                        currentFactory = factory;
                        logger.info("create ConnectionFactory. " + config);
                    } else {
                        currentFactory = exist;
                    }
                }
            }
        }
        return currentFactory;
    }

    public static RedisConnectionFactory getConnectionFactory(RedisConfig jedisFactoryConfig,
            JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(jedisFactoryConfig.getHostName());
        factory.setPort(jedisFactoryConfig.getPort());
        factory.setPassword(jedisFactoryConfig.getPassword());
        factory.setPoolConfig(jedisPoolConfig);
        factory.setDatabase(jedisFactoryConfig.getDbIndex());
        factory.afterPropertiesSet();
        return factory;
    }

    public static <K, V> RedisTemplate<K, V> createRedisTemplate(String key) {
        RedisConnectionFactory factory = getConnectionFactory(key);
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<K, V>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    public static StringRedisTemplate createStringRedisTemplate(String key) {
        RedisConnectionFactory factory = getConnectionFactory(key);
        StringRedisTemplate redisTemplate = new StringRedisTemplate(factory);
        return redisTemplate;
    }

}
