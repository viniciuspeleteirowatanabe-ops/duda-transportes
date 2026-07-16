package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // Esta linha mágica é o que liga o servidor Tomcat na porta 8080!
        SpringApplication.run(DemoApplication.class, args);
    }
    
}