package com.codeyn.resouce.bus.redis;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;

import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionFactoryBean implements FactoryBean<RedisConnectionFactory>, InitializingBean {

    private String key;

    private JedisPoolConfig jedisPoolConfig;

    private RedisConnectionFactory connectionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(key, "argument 'key' must not be null!");
        if (jedisPoolConfig == null) {
            jedisPoolConfig = new JedisPoolConfig();
        }
        connectionFactory = RedisFactory.getConnectionFactory(key, jedisPoolConfig);
    }

    @Override
    public RedisConnectionFactory getObject() throws Exception {
        return connectionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return JedisConnectionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }
    
}
