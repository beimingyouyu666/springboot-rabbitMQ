package com.yangk.consumer.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author yangkun
 * @Date 2019/7/22
 * @Version 1.0
 */
@Configuration
@Slf4j
public class YkAConsumer {

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Resource
    private ConnectionFactory ykConnectionFactory;

    //    @Bean
    public SimpleMessageListenerContainer ykAMessageContainer() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "YKA_R_DEAD");
        map.put("x-dead-letter-routing-key", "YKA_X_DEAD");
        Queue queue = new Queue("YKA_Q", true, false, false, map);

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
                log.info("----------------------------------------------yka consumer body is:{}", body);
                //确认消息成功消费
                channel.basicAck(deliveryTag, false);
                isReject = false;
            } catch (Exception e) {
                log.error("----------------------------------------------yka consumer error! body:{}", body, e);
            } finally {
                if (isReject) {
                    log.info("----------------------------------------------yka consumer set reject!body:{}", body);
                    channel.basicReject(deliveryTag, false);
                }
            }
        });
        return container;
    }
}
