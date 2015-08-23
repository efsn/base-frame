package com.codeyn.resouce.bus.mq.config;

import java.util.ArrayList;
import java.util.List;

public class SubscribeConfig {

    private final String mqSpace;
    private String queueName;
    private int concurrentNum = 3;
    private boolean autoDeclare = true;
    private boolean immediateReply = false;
    private boolean isTemporary = false;
    private List<QueueBinding> bindings = new ArrayList<>();

    public SubscribeConfig(String mqSpace, List<QueueBinding> bindings) {
        this.mqSpace = mqSpace;
        this.bindings = bindings;
        this.isTemporary = true;
    }

    public SubscribeConfig(String mqSpace, String queueName) {
        this.mqSpace = mqSpace;
        this.queueName = queueName;
        this.isTemporary = false;
    }

    public String getMqSpace() {
        return mqSpace;
    }

    public String getQueueName() {
        return queueName;
    }

    public boolean isImmediateReply() {
        return immediateReply;
    }

    public void setImmediateReply(boolean immediateReply) {
        this.immediateReply = immediateReply;
    }

    public int getConcurrentNum() {
        return concurrentNum;
    }

    public void setConcurrentNum(int concurrentNum) {
        if (concurrentNum > 0) {
            this.concurrentNum = concurrentNum;
        }
    }

    public boolean isAutoDeclare() {
        return autoDeclare;
    }

    public void setAutoDeclare(boolean autoDeclare) {
        this.autoDeclare = autoDeclare;
    }

    public List<QueueBinding> getBindings() {
        return bindings;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    @Override
    public String toString() {
        return String.format(
                "SubscribeConfig [mqSpace=%s, queueName=%s, autoDeclare=%s, concurrentNum=%s, immediateReply=%s]",
                mqSpace, queueName, autoDeclare, concurrentNum, immediateReply);
    }

}
