package com.codeyn.resouce.bus.mq;

import org.springframework.amqp.core.MessageProperties;

public class MsgPackage<T> {

    private MessageProperties messageProperties;

    private T messageBody;

    public MsgPackage(MessageProperties messageProperties, T messageBody) {
        this.messageProperties = messageProperties;
        this.messageBody = messageBody;
    }

    public MessageProperties getMessageProperties() {
        return messageProperties;
    }

    public T getMessageBody() {
        return messageBody;
    }
    
}
