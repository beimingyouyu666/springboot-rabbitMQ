package com.yangk.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsumerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Value("${spring.rabbitmq.default.addresses}")
    private String addresses;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("----" + addresses);
    }
}
