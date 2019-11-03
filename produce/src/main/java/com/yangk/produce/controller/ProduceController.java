package com.yangk.produce.controller;

import com.yangk.produce.rabbitmq.YkProduce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author yangkun
 * @Date 2019/7/23
 * @Version 1.0
 */
@RestController
public class ProduceController {

    @Autowired
    private YkProduce ykProduce;

    @RequestMapping("/produceYkQueue")
    public void produceYkQueue(String msg, String delay) {
        if (delay != null) {
            ykProduce.sendDelay(msg, delay);
        } else {
            ykProduce.send(msg);
        }
    }

}
