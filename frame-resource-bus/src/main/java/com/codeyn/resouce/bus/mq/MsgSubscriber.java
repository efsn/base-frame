package com.codeyn.resouce.bus.mq;

import com.codeyn.resouce.bus.mq.config.SubscribeConfig;

public abstract class MsgSubscriber<T> implements IMsgHandler<T> {

    private SubscribeConfig config;

    public MsgSubscriber() {
        config = this.buildSubscribeConfig();
        if (config == null) {
            throw new RuntimeException("call buildSubscribeConfig() return null.");
        }
        MqFactory.addMsgHandler(config, this);
    }

    abstract public SubscribeConfig buildSubscribeConfig();

    public SubscribeConfig getConfig() {
        return config;
    }
    
}
