package com.yangk.consumer.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description rabbitmq消费者配置文件
 * @Author yangkun
 * @Date 2019/7/19
 * @Version 1.0
 */
@Configuration
public class MqConsumerConfig {

    @Bean
    @Primary
    public ConnectionFactory ykConnectionFactory(
            @Value("${spring.rabbitmq.default.addresses}") String addresses,
            @Value("${spring.rabbitmq.default.username}") String username,
            @Value("${spring.rabbitmq.default.password}") String password,
            @Value("${spring.rabbitmq.default.virtual-host}") String virtualHost
    ) {
        CachingConnectionFactory connectionFactory = getConnectionFactory(addresses, username, password, virtualHost);
        connectionFactory.setRequestedHeartBeat(12);
//        connectionFactory.setPublisherConfirms(false);
        return connectionFactory;
    }

    /**
     * 初始化RabbitMQ 连接池
     *
     * @param addresses
     * @param username
     * @param password
     * @param virtualHost
     * @return
     */
    private CachingConnectionFactory getConnectionFactory(String addresses, String username, String password,
                                                          String virtualHost) {
        //这里指定localhost是因为linux下取不到默认配置
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        //必须要设置才能进行消息的回调(消息确认机制)
        connectionFactory.setPublisherConfirms(true);
        //命名空间的作用，需要自己创建
        connectionFactory.setVirtualHost(virtualHost);
        return connectionFactory;
    }

    @Bean
    @Scope("prototype")
    @Primary
    public RabbitTemplate ykTemplate(@Qualifier("ykConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setChannelTransacted(true);
        return template;
    }

    @Bean
    public RabbitAdmin ykRabbitAdmin(@Qualifier("ykConnectionFactory") ConnectionFactory connectionFactory) {
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        initYkQueue(rabbitAdmin);
//        initYkAQueue(rabbitAdmin);
        return rabbitAdmin;
    }

    /**
     * 定义生产者数据队列
     *
     * @param rabbitAdmin
     */
    private void initYkQueue(RabbitAdmin rabbitAdmin) {
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-routing-key", "YK_R_DEAD");
        map.put("x-dead-letter-exchange", "YK_X_DEAD");
        map.put("x-message-ttl", 10000);
        Queue queue = new Queue("YK_Q", true, false, false, map);
        DirectExchange exchange = new DirectExchange("YK_X", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("YK_R"));
        //定义预报的死信队列(若配置转发生产者不需要定义死信队列)
        Queue deadQueue = new Queue("YK_Q_DEAD", true, false, false);
        DirectExchange deadExchange = new DirectExchange("YK_X_DEAD", true, false);
        rabbitAdmin.declareQueue(deadQueue);
        rabbitAdmin.declareExchange(deadExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(deadQueue).to(deadExchange).with("YK_R_DEAD"));
    }

    private void initYkAQueue(RabbitAdmin rabbitAdmin) {
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-routing-key", "YKA_R_DEAD");
        map.put("x-dead-letter-exchange", "YKA_X_DEAD");
        Queue queue = new Queue("YKA_Q", true, false, false, map);
        DirectExchange exchange = new DirectExchange("YKA_X", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("YKA_R"));
        //定义预报的死信队列(若配置转发生产者不需要定义死信队列)
        Queue deadQueue = new Queue("YKA_Q_DEAD", true, false, false);
        DirectExchange deadExchange = new DirectExchange("YKA_X_DEAD", true, false);
        rabbitAdmin.declareQueue(deadQueue);
        rabbitAdmin.declareExchange(deadExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(deadQueue).to(deadExchange).with("YKA_R_DEAD"));
    }

}
