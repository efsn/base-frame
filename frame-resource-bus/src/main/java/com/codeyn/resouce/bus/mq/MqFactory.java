package com.codeyn.resouce.bus.mq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import com.codeyn.resouce.bus.ConfigLoader;
import com.codeyn.resouce.bus.mq.config.MqExchangeConfig;
import com.codeyn.resouce.bus.mq.config.MqQueueConfig;
import com.codeyn.resouce.bus.mq.config.MqSpaceConfig;
import com.codeyn.resouce.bus.mq.config.PublishConfig;
import com.codeyn.resouce.bus.mq.config.QueueBinding;
import com.codeyn.resouce.bus.mq.config.SubscribeConfig;

public class MqFactory {

    private static final Logger logger = LoggerFactory.getLogger(MqFactory.class);

    private static ConcurrentHashMap<String, ConnectionFactory> factoryCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Exchange> exchangeCache = new ConcurrentHashMap<>();
    private static List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (SimpleMessageListenerContainer container : containers) {
                    container.stop();
                }
            }
        }));
    }

    public static RabbitTemplate createPubTemplate(PublishConfig publishConfig) {
        Assert.notNull(publishConfig, "'publishConfig' is required; it must not be null!");

        logger.info("create MQ publisher." + publishConfig);
        ConnectionFactory factory = getConnectionFactory(publishConfig.getMqSpace());
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);

        String exchangeName = publishConfig.getExechangeName();
        rabbitTemplate.setExchange(exchangeName);
        if (publishConfig.getRetryTimes() > 0) {
            // 重试策略
            RetryTemplate retryTemplate = new RetryTemplate();
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(500);
            backOffPolicy.setMultiplier(2);
            backOffPolicy.setMaxInterval(30000);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(publishConfig.getRetryTimes(), Collections
                    .<Class<? extends Throwable>, Boolean> singletonMap(Exception.class, true)));
            rabbitTemplate.setRetryTemplate(retryTemplate);
        }
        return rabbitTemplate;
    }

    public static void addMsgHandler(SubscribeConfig subscribeConfig, IMsgHandler<?> msgHandler) {
        Assert.notNull(subscribeConfig, "'subscribeConfig' is required; it must not be null!");
        Assert.notNull(msgHandler, "'msgHandler' is required; it must not be null!");
        logger.info(subscribeConfig.toString());
        String queueName = null;
        String mqSpace = subscribeConfig.getMqSpace();
        ConnectionFactory factory = getConnectionFactory(mqSpace);
        if (subscribeConfig.isTemporary()) {
            queueName = UUID.randomUUID().toString();
            RabbitAdmin admin = new RabbitAdmin(factory);
            Queue queue = new Queue(queueName, false, true, true);
            admin.declareQueue(queue);
            declareQueue(mqSpace, admin, queue, subscribeConfig.getBindings());
            logger.info(String.format("add MQ msg handler for queue. queueName=%s, mqSpace=%s, bindings=%s", queueName,
                    mqSpace, subscribeConfig.getBindings()));
        } else if (subscribeConfig.isAutoDeclare()) {
            queueName = subscribeConfig.getQueueName();
            MqQueueConfig queueConfig = ConfigLoader.getMqQueueConfig(mqSpace, queueName);
            if (queueConfig == null) {
                throw new IllegalStateException(" can not find queueConfig by queueName: " + queueName);
            }
            RabbitAdmin admin = new RabbitAdmin(factory);
            admin.setIgnoreDeclarationExceptions(true);
            Queue queue = new Queue(queueName, true, false, false);
            declareQueue(mqSpace, admin, queue, queueConfig.getBindings());
            logger.info(String.format("add MQ msg handler for queue. queueName=%s, mqSpace=%s, bindings=%s", queueName,
                    mqSpace, queueConfig.getBindings()));
        }

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        MessageListenerAdapter adapter = null;
        try {
            if (msgHandler.getClass().getMethod("handleMessage", MsgPackage.class) != null) {
                adapter = new MessageListenerAdapter(msgHandler, new SimpleMessageConverter() {
                    @Override
                    public Object fromMessage(Message message) throws MessageConversionException {
                        return new MsgPackage<Object>(message.getMessageProperties(), super.fromMessage(message));
                    }
                });
            }
        } catch (Exception e) {
        }
        if (adapter == null) {
            adapter = new MessageListenerAdapter(msgHandler);
        }
        container.setMessageListener(adapter);
        container.setQueueNames(queueName);
        container.setConcurrentConsumers(subscribeConfig.getConcurrentNum());
        // 发生异常不需要重新加入队列，防止死循环
        container.setDefaultRequeueRejected(false);
        if (subscribeConfig.isImmediateReply()) {
            container.setChannelTransacted(false);
            container.setAcknowledgeMode(AcknowledgeMode.NONE);
        } else {
            /*
             * container.setChannelTransacted(true);
             * container.setTransactionManager(new
             * RabbitTransactionManager(factory));
             */
        }
        container.start();
        containers.add(container);
    }

    private static void declareQueue(String mqSpace, RabbitAdmin admin, Queue queue, List<QueueBinding> bindings) {
        admin.declareQueue(queue);
        if (bindings != null) {
            for (QueueBinding binding : bindings) {
                Exchange exchange = buildExchange(mqSpace, binding.getExchangeName(), queue.isDurable());
                admin.declareExchange(exchange);
                admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(binding.getPattern()).noargs());
            }
        }
    }

    public static ConnectionFactory getConnectionFactory(String mqSpace) {
        Assert.notNull(mqSpace, "'mqSpace' is required; it must not be null!");
        ConnectionFactory currentFactory = factoryCache.get(mqSpace);
        if (currentFactory == null) {
            synchronized (MqFactory.class) {
                currentFactory = factoryCache.get(mqSpace);
                if (currentFactory == null) {
                    MqSpaceConfig factoryConfig = ConfigLoader.getMqSpaceConfig(mqSpace);
                    if (factoryConfig == null) {
                        throw new IllegalStateException(String.format(
                                " can not find factoryConfig. mqSpace=%s, env=%s", mqSpace, System.getProperty("env")));
                    }
                    CachingConnectionFactory cf = new CachingConnectionFactory(factoryConfig.getHost(),
                            factoryConfig.getPort());
                    if (factoryConfig.getAddresses() != null) {
                        cf.setAddresses(factoryConfig.getAddresses());
                    }
                    cf.setUsername(factoryConfig.getUsername());
                    cf.setPassword(factoryConfig.getPassword());
                    if (factoryConfig.getVhost() != null) {
                        cf.setVirtualHost(factoryConfig.getVhost());
                    }
                    ConnectionFactory exist = factoryCache.putIfAbsent(mqSpace, cf);
                    if (exist == null) {
                        currentFactory = cf;
                        logger.info("create ConnectionFactory. " + factoryConfig);
                    } else {
                        currentFactory = exist;
                    }
                }
            }
        }
        return currentFactory;
    }

    public static Exchange buildExchange(String mqSpace, String exchangeName, boolean isDurable) {
        boolean isAutoDelete = !isDurable;
        Exchange exchange = exchangeCache.get(exchangeName);
        if (exchange == null) {
            MqExchangeConfig exchangeConfig;
            if (MqExchangeConfig.DEFAULT_NAME.equals(exchangeName)) {
                exchangeConfig = MqExchangeConfig.DEFAULT_CONFIG;
            } else {
                exchangeConfig = ConfigLoader.getMqExchangeConfig(mqSpace, exchangeName);
            }
            if (exchangeConfig == null) {
                throw new IllegalStateException(String.format(
                        " can not find exchangeConfig. exchangeName=%s, mqSpace=%s", exchangeName, mqSpace));
            }
            switch (exchangeConfig.getType()) {
                case ExchangeTypes.DIRECT:
                    exchange = new DirectExchange(exchangeName, isDurable, isAutoDelete);
                    break;
                case ExchangeTypes.TOPIC:
                    exchange = new TopicExchange(exchangeName, isDurable, isAutoDelete);
                    break;
                case ExchangeTypes.FANOUT:
                    exchange = new FanoutExchange(exchangeName, isDurable, isAutoDelete);
                    break;
                case ExchangeTypes.HEADERS:
                    exchange = new HeadersExchange(exchangeName, isDurable, isAutoDelete);
                    break;
                default:
                    throw new IllegalStateException(" can not find exchange by exchangeType: "
                            + exchangeConfig.getType());
            }
            Exchange exist = exchangeCache.putIfAbsent(exchangeName, exchange);
            exchange = exist == null ? exchange : exist;
        }
        return exchange;
    }

}
