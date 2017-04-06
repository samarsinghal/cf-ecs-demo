package com.gopivotal.cf.samples.s3.config;

import com.gopivotal.cf.samples.s3.repository.S3;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {

    @Bean
    public S3 s3() {
        return connectionFactory().service(S3.class);
    }

}
