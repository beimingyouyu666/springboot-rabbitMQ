package com.yangk.produce.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;

/**
 * @Description TODO
 * @Author yangkun
 * @Date 2019/7/19
 * @Version 1.0
 */
@Slf4j
@Component
public class YkProduce {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param message
     */
    public void send(String message) {
        try {
            log.info("----------------------------------------");
            log.info("produce send body is:{}", message);
            rabbitTemplate.convertAndSend("YK_X", "YK_R", message);
        } catch (AmqpException e) {
            log.error("send fail!", e);
        }
    }

    /**
     * 发送延时消息
     *
     * @param message
     */
    public void sendDelay(String message, String expiration) {
        try {
            message = message + Calendar.getInstance().getTime();
            log.info("produce send body is:{},and delay is:{}", message, expiration);
            rabbitTemplate.convertAndSend("YK_X", "YK_R", message, msg -> {
                msg.getMessageProperties().setExpiration(expiration);
                return msg;
            });
        } catch (AmqpException e) {
            log.error("send fail!", e);
        }
    }

}
