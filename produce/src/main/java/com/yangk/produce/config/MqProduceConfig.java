package com.yangk.produce.config;

import lombok.extern.slf4j.Slf4j;
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

import java.util.*;

/**
 * @Description rabbitmq生产者配置文件
 * @Author yangkun
 * @Date 2019/7/19
 * @Version 1.0
 */
@Configuration
@Slf4j
public class MqProduceConfig {

    @Bean
    @Primary
    public ConnectionFactory ykConnectionFactory(
            @Value("${spring.rabbitmq.default.addresses}") String addresses,
            @Value("${spring.rabbitmq.default.username}") String username,
            @Value("${spring.rabbitmq.default.password}") String password,
            @Value("${spring.rabbitmq.default.virtual-host}") String virtualHost
    ) {
        CachingConnectionFactory connectionFactory = getConnectionFactory(addresses, username, password, virtualHost);
        // 将心跳超时设置为12秒 参考：https://www.cnblogs.com/Tommy-Yu/p/5775852.html
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
        //TODO 命名空间的作用，需要自己创建 https://blog.csdn.net/drcwr/article/details/50846134
        connectionFactory.setVirtualHost(virtualHost);
        //必须要设置才能进行消息的回调(消息确认机制)
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope("prototype")
    @Primary
    public RabbitTemplate ykTemplate(@Qualifier("ykConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //事务与回调机制是冲突的，不能同时设置
//        template.setChannelTransacted(true);
        /*template.setConfirmCallback((correlationData,ack,cause)->{
            if (ack){
                log.info("消息id为：{}，被消费成功了",correlationData);
            }else {
                log.info("消息id为：{}，被消费失败了，失败原因：{}",correlationData,cause);
            }
        });*/
        return template;
    }

    @Bean
    @Scope("prototype")
    public RabbitTemplate ykATemplate(@Qualifier("ykConnectionFactory") ConnectionFactory connectionFactory) {
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
        map.put("x-dead-letter-exchange", "YK_X_DEAD");
        map.put("x-dead-letter-routing-key", "YK_R_DEAD");
        map.put("x-message-ttl", 10000);

        Queue queue = new Queue("YK_Q", true, false, false, map);
        DirectExchange exchange = new DirectExchange("YK_X", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("YK_R"));
    }

    /**
     * 定义生产者数据队列
     *
     * @param rabbitAdmin
     */
    private void initYkAQueue(RabbitAdmin rabbitAdmin) {
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "YKA_X_DEAD");
        map.put("x-dead-letter-routing-key", "YKA_R_DEAD");
        Queue queue = new Queue("YKA_Q", true, false, false, map);
        DirectExchange exchange = new DirectExchange("YKA_X", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("YKA_R"));
    }

}
