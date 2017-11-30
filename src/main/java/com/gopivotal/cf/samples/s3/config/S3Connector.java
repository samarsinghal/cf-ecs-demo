package com.gopivotal.cf.samples.s3.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3Connector {

    private AmazonS3 client;
    private String bucket;

    public S3Connector(S3Properties s3Properties) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(
                s3Properties.getAccessKey(),
                s3Properties.getSecretKey()
        );

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    s3Properties.getEndpoint(), "Standard"
                ));

        AmazonS3 amazonS3 = builder.build();
        this.client = amazonS3;
        this.bucket = s3Properties.getBucket();
    }

    public AmazonS3 getClient() {
        return client;
    }

    public String getBucket() {
        return bucket;
    }

}
