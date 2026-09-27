package com.unishut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class UniShutApplication {

    public static void main(String[] args) {

        SpringApplication.run(UniShutApplication.class, args);

        System.out.println("--------------------------------------");
        System.out.println(" uniShut Backend Server Started ");
        System.out.println(" Spring Boot Application Running ");
        System.out.println("--------------------------------------");
    }

}