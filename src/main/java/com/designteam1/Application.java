package com.designteam1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @PostConstruct
    public void postConstruct() {
        System.setProperty("spring.data.mongodb.uri", "mongodb://gplsUser:8NNRPQyXXEQVTEpncndVdD1Ep7XDyyRoOQHVWrmzJ9tmel1g03@34.203.30.164:27017/gpls");
    }

    public static void main(String[] args) {
        logger.info("GPLS API is starting...");
        SpringApplication.run(Application.class, args);
    }
}
