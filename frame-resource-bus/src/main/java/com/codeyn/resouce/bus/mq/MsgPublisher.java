package com.codeyn.resouce.bus.mq;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.codeyn.resouce.bus.mq.config.PublishConfig;

public class MsgPublisher<T extends Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(MsgPublisher.class);

    protected String defaultRouteKey = "";
    protected RabbitTemplate rabbitTemplate;

    private MessagePostProcessor messagePostProcessor;

    public MsgPublisher(PublishConfig publishConfig) {
        this.rabbitTemplate = MqFactory.createPubTemplate(publishConfig);
        this.messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setTimestamp(new Date());
                return message;
            }
        };
    }

    public MsgPublisher(String defaultRouteKey, PublishConfig publishConfig) {
        this(publishConfig);
        this.defaultRouteKey = defaultRouteKey;
    }

    public void sendMsg(T message) {
        sendMsg(defaultRouteKey, message);
    }

    public void sendMsg(String routeKey, T message) {
        String messageKey = getMessageKey(routeKey, message);
        rabbitTemplate.convertAndSend(messageKey, message, messagePostProcessor);
        logger.info("send msg to MQ success. " + message);
    }

    protected String getMessageKey(String routeKey, T message) {
        return routeKey;
    }

    public RabbitTemplate getTemplate() {
        return rabbitTemplate;
    }

    public String getDefaultRouteKey() {
        return defaultRouteKey;
    }
}
