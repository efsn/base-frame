package com.codeyn.resouce.bus;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.codeyn.resouce.bus.ds.DataSourceConfig;
import com.codeyn.resouce.bus.mq.config.MqExchangeConfig;
import com.codeyn.resouce.bus.mq.config.MqQueueConfig;
import com.codeyn.resouce.bus.mq.config.MqSpaceConfig;
import com.codeyn.resouce.bus.redis.RedisConfig;
import com.sqq.zk.core.ZkEngine;

public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String ROOT_SCOPE = "/codeyn";
    private static final String MQ_SCOPE = "/mq";
    private static final String REDIS_SCOPE = "/redis";
    private static final String DS_SCOPE = "/ds";
    private static final String URL_SCOPE = "/url";

    private static final boolean LOCAL_ENABLE;
    private static final String LOCAL_CONFIG_PATH = "resource-dev.properties";
    private static Map<String, String> localCfg = new HashMap<>();

    static {
        LOCAL_ENABLE = System.getProperty("env", "debug").equals("debug");
        if (LOCAL_ENABLE) {
            try {
                InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(LOCAL_CONFIG_PATH);
                if (in != null) {
                    Properties prop = new Properties();
                    prop.load(in);
                    Map<String, JSONObject> map = new HashMap<>();
                    for (String key : prop.stringPropertyNames()) {
                        String val = prop.getProperty(key);
                        int i = key.indexOf(".");
                        if (i < 0) continue;

                        String path = key.substring(0, i);
                        String name = key.substring(i + 1);
                        path = path.replace('.', '/');
                        JSONObject json = map.get(path);
                        if (json == null) {
                            json = new JSONObject();
                            map.put(path, json);
                        }
                        json.put(name, val);
                    }
                    for (String key : map.keySet()) {
                        JSONObject json = map.get(key);
                        if (!json.isEmpty()) {
                            localCfg.put("/" + key, json.toJSONString());
                        }
                    }
                }

            } catch (IOException e) {
                logger.error("load local config error", e);
            }
        }
    }

    public static DataSourceConfig getDsCfg(String name) {
        name = DS_SCOPE + (name.startsWith("/") ? name : "/".concat(name));
        String nodeData = getCfgData(name);
        return JSON.parseObject(nodeData, DataSourceConfig.class);
    }

    public static RedisConfig getJedisFactoryConfig(String name) {
        name = REDIS_SCOPE + (name.startsWith("/") ? name : "/".concat(name));
        RedisConfig result;
        String nodeData = getCfgData(name);
        JSONObject config = JSON.parseObject(nodeData);
        String hostName = config.getString("hostName");
        Integer port = config.getInteger("port");
        if (port == null) {
            result = new RedisConfig(hostName);
        } else {
            result = new RedisConfig(hostName, port);
        }
        Integer dbIndex = config.getInteger("dbIndex");
        if (dbIndex != null) {
            result.setDbIndex(dbIndex);
        }
        String password = config.getString("password");
        if (password != null) {
            result.setPassword(password);
        }
        return result;
    }

    public static MqSpaceConfig getMqSpaceConfig(String mqSpace) {
        String nodeData = getCfgData(MQ_SCOPE + "/" + mqSpace);
        MqSpaceConfig cf = JSON.parseObject(nodeData, MqSpaceConfig.class);
        cf.setVhost(mqSpace);
        return cf;
    }

    public static MqExchangeConfig getMqExchangeConfig(String mqSpace, String exchangeName) {
        String nodeData = getCfgData(MQ_SCOPE + "/" + mqSpace + "/exchange/" + exchangeName);
        MqExchangeConfig cf = JSON.parseObject(nodeData, MqExchangeConfig.class);
        cf.setName(exchangeName);
        return cf;
    }

    public static MqQueueConfig getMqQueueConfig(String mqSpace, String queueName) {
        String nodeData = getCfgData(MQ_SCOPE + "/" + mqSpace + "/queue/" + queueName);
        MqQueueConfig cf = JSON.parseObject(nodeData, MqQueueConfig.class);
        cf.setName(queueName);
        return cf;
    }

    public static String getUrl(String name) {
        String url = getCfgData(URL_SCOPE + "/" + name);
        return url;
    }

    private static String getCfgData(String path) {
        if (!path.startsWith("/")) {
            path = "/".concat(path);
        }

        if (LOCAL_ENABLE) {
            String data = localCfg.get(path);
            if (StringUtils.isEmpty(data)) {
                ZkEngine zkEngine = ZkContext.tryGetZkEngine();
                if (zkEngine != null) {
                    data = zkEngine.getNodeDataAsString(ROOT_SCOPE + path);
                }
            }
            return data;
        } else {
            return ZkContext.getZkEngine().getNodeDataAsString(ROOT_SCOPE + path);
        }
    }

}
