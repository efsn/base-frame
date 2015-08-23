package com.codeyn.resouce.bus.mq.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MqQueueConfig {

    private String name;
    private List<QueueBinding> bindings = new ArrayList<>();

    public MqQueueConfig() {
    }

    public MqQueueConfig(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addBinding(String exchangeName, String pattern) {
        this.bindings.add(new QueueBinding(exchangeName, pattern));
    }

    public List<QueueBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    public void setBindings(List<QueueBinding> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "QueueConfig [name=" + name + ", bindings="
                + (bindings != null ? bindings.subList(0, Math.min(bindings.size(), maxLen)) : null) + "]";
    }

}
