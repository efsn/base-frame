package com.codeyn.resouce.bus.mq.config;

public class QueueBinding {

    private String exchangeName;
    private String pattern;

    public QueueBinding() {
    }

    public QueueBinding(String exchangeName, String pattern) {
        this.exchangeName = exchangeName;
        this.pattern = pattern;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return String.format("[exchangeName=%s, pattern=%s]", exchangeName, pattern);
    }

}
