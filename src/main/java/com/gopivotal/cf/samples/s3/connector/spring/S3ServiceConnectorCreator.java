package com.gopivotal.cf.samples.s3.connector.spring;

import com.gopivotal.cf.samples.s3.connector.cloudfoundry.S3ServiceInfo;
import com.gopivotal.cf.samples.s3.repository.S3;
import org.springframework.cloud.service.AbstractServiceConnectorCreator;
import org.springframework.cloud.service.ServiceConnectorConfig;

public class S3ServiceConnectorCreator extends AbstractServiceConnectorCreator<S3, S3ServiceInfo> {

    @Override
    public S3 create(S3ServiceInfo serviceInfo, ServiceConnectorConfig serviceConnectorConfig) {
        return new S3(serviceInfo);
    }
}
