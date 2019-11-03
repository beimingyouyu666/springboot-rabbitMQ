package com.yangk.consumer.rabbitmq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author yangkun
 * @Date 2019/7/19
 * @Version 1.0
 */
@Configuration
@Slf4j
public class YkConsumer {

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Resource
    private ConnectionFactory ykConnectionFactory;

//    @Bean

    /**
     * 监听正常队列
     *
     * @return
     */
    public SimpleMessageListenerContainer ykMessageContainer() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "YK_R_DEAD");
        map.put("x-dead-letter-routing-key", "YK_X_DEAD");
        Queue queue = new Queue("YK_Q", true, false, false, map);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(ykConnectionFactory);
        container.setQueues(queue);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(3);
        container.setConcurrentConsumers(3);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final long deliveryTag = message.getMessageProperties().getDeliveryTag();
            final String body = new String(message.getBody(), DEFAULT_CHARSET);
            boolean isReject = true;
            try {
                //消费数据
                log.info("-------------------------------------------------------");
                log.info("yk consumer body is:{},time is :{}", body, Calendar.getInstance().getTime());
//                int i = 0/0;
                //确认消息成功消费
                channel.basicAck(deliveryTag, false);
                isReject = false;
            } catch (Exception e) {
                log.error("----------------------------------------------yk consumer error! body:{}", body, e);
            } finally {
                if (isReject) {
                    log.info("----------------------------------------------yk consumer set reject!body:{}", body);
                    channel.basicReject(deliveryTag, false);
                }
            }
        });
        return container;
    }

    @Bean
    /**
     * 监听死信队列，要让这个起作用，得不监听正常队列
     */
    public SimpleMessageListenerContainer ykTtlMessageContainer() {
        Queue queue = new Queue("YK_Q_DEAD", true, false, false);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(ykConnectionFactory);
        container.setQueues(queue);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(3);
        container.setConcurrentConsumers(3);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final long deliveryTag = message.getMessageProperties().getDeliveryTag();
            final String body = new String(message.getBody(), DEFAULT_CHARSET);
            boolean isReject = true;
            try {
                //消费数据
                log.info("------------------------------------------------------");
                log.info("yk consumer body is:{},time is :{}", body, Calendar.getInstance().getTime());
//                int i = 0/0;
                //确认消息成功消费
                channel.basicAck(deliveryTag, false);
                isReject = false;
            } catch (Exception e) {
                log.error("---------------------------------------------yk consumer error! body:{}", body, e);
            } finally {
                if (isReject) {
                    log.info("---------------------------------------------yk consumer set reject!body:{}", body);
                    channel.basicReject(deliveryTag, false);
                }
            }
        });
        return container;
    }
}
