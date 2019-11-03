package com.yangk.produce;

import com.yangk.produce.rabbitmq.YkAProduce;
import com.yangk.produce.rabbitmq.YkProduce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProduceApplication implements CommandLineRunner {

    @Autowired
    private YkProduce ykProduce;

    @Autowired
    private YkAProduce ykAProduce;

    public static void main(String[] args) {
        SpringApplication.run(ProduceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        ykProduce.sendDelay("hello yk consumer!"+ Calendar.getInstance().getTime(),"5000");
        /*while (true){
            ykProduce.send("hello yk consumer!"+ Calendar.getInstance().getTime());
            ykAProduce.send(Calendar.getInstance().getTime()+"---------------------------------------");
            Thread.sleep(1000);
        }*/
    }
}
