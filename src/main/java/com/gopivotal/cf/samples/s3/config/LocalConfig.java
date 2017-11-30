package com.gopivotal.cf.samples.s3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
@EnableConfigurationProperties({S3Properties.class})
public class LocalConfig {

    @Autowired
    S3Properties s3Properties;

    @Bean
    public S3Connector s3() {
        return new S3Connector(s3Properties);
    }

}
