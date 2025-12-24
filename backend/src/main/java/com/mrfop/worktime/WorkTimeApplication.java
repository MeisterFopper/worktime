package com.mrfop.worktime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WorkTimeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkTimeApplication.class, args);
    }
}