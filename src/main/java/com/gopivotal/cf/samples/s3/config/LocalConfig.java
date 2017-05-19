package com.gopivotal.cf.samples.s3.config;

import com.gopivotal.cf.samples.s3.connector.cloudfoundry.S3ServiceInfo;
import com.gopivotal.cf.samples.s3.repository.S3;
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
    private S3Properties s3Properties;

    @Bean
    public S3 s3() {
        S3ServiceInfo serviceInfo = new S3ServiceInfo("", s3Properties.getAccessKey(), s3Properties.getSecretKey(), s3Properties.getBucket(),
                s3Properties.getRegion(), s3Properties.getEndpoint(), s3Properties.getPathStyleAccess(), s3Properties.getBaseUrl(),
                s3Properties.getUsePresignedUrls());
        return new S3(serviceInfo);
    }

}
