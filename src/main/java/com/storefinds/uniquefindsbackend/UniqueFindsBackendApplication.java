package com.storefinds.uniquefindsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UniqueFindsBackendApplication {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Bootstrap Spring Boot application.
     * Params:
     * - args: runtime startup arguments
     * Returns: None
     * Throws: None
     */
    public static void main(String[] args) {
        SpringApplication.run(UniqueFindsBackendApplication.class, args);
    }

}