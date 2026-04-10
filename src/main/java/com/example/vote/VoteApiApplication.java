package com.example.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoteApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoteApiApplication.class, args);
    }
}
