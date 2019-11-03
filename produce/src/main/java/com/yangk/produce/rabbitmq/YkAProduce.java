package com.yangk.produce.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author yangkun
 * @Date 2019/7/22
 * @Version 1.0
 */
@Slf4j
@Component
public class YkAProduce {

    @Autowired
    @Qualifier("ykATemplate")
    private RabbitTemplate rabbitTemplate;

    public void send(String message) {
        try {
            log.info("yka produce send success:{}" + message);
            rabbitTemplate.convertAndSend("YKA_X", "YKA_R", message);
        } catch (AmqpException e) {
            log.error("yka produce send fail:{}" + message);
        }
    }

}
