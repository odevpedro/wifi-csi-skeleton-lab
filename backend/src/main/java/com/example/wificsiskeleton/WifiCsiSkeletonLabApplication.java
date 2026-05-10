package com.example.wificsiskeleton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WifiCsiSkeletonLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifiCsiSkeletonLabApplication.class, args);
    }
}
