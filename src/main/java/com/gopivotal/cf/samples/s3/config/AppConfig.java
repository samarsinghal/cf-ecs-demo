package com.gopivotal.cf.samples.s3.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.cloudfoundry.CloudFoundryConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.gopivotal.cf.samples.s3")
public class AppConfig {

    public static void main(String[] args) {
        if (new CloudFoundryConnector().isInMatchingCloud()) {
            System.setProperty("spring.profiles.active", "cloud");
        }
        SpringApplication.run(AppConfig.class, args);
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }
}
