package com.codeyn.resouce.bus.mq.config;

public class MqExchangeConfig {

    public static final String DEFAULT_NAME = "_default_exchange";
    public static final MqExchangeConfig DEFAULT_CONFIG = new MqExchangeConfig(DEFAULT_NAME, "topic");

    private String name;
    private String type;

    public MqExchangeConfig() {
    }

    public MqExchangeConfig(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
