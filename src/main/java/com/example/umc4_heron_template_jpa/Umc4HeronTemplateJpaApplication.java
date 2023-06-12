package com.example.umc4_heron_template_jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Umc4HeronTemplateJpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(Umc4HeronTemplateJpaApplication.class, args);
    }
}
