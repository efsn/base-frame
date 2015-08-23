package com.codeyn.resouce.bus.mq.config;

public class PublishConfig {

    private final String mqSpace;
    private String exechangeName = MqExchangeConfig.DEFAULT_NAME;
    private int retryTimes = 2; // 默认重试次数

    public void setExechangeName(String exechangeName) {
        this.exechangeName = exechangeName;
    }

    public PublishConfig(String mqSpace) {
        this.mqSpace = mqSpace;
    }

    public PublishConfig(String mqSpace, String exechangeName) {
        this.mqSpace = mqSpace;
        this.exechangeName = exechangeName;
    }

    public String getMqSpace() {
        return mqSpace;
    }

    public String getExechangeName() {
        return exechangeName;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public String toString() {
        return "PublishConfig [mqSpace=" + mqSpace + ", exechangeName=" + exechangeName + ", retryTimes=" + retryTimes
                + "]";
    }

}
