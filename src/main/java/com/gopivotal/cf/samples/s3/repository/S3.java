package com.gopivotal.cf.samples.s3.repository;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.gopivotal.cf.samples.s3.config.S3Properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3 {

    private AmazonS3 amazonS3;
    private String bucket;

    public S3(S3Properties s3Properties) {
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
        this.amazonS3 = amazonS3;
        this.bucket = s3Properties.getBucket();
    }

    public void put(String filename, File content) throws MalformedURLException {
        String key = String.format("%s-%s", UUID.randomUUID().toString(), filename);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, content)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(putObjectRequest);
    }

    public Iterable<S3File> list() {
        Pattern p = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}-(.*)");
        ObjectListing listing = amazonS3.listObjects(bucket);
        ArrayList<S3File> files = new ArrayList<>();
        for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
            S3File f = new S3File(objectSummary.getKey());
            Matcher m = p.matcher(objectSummary.getKey());
            if (m.find()) {
                f.setName(m.group(1));
            }else {
                f.setName(objectSummary.getKey());
            }
            f.setDate(objectSummary.getLastModified());
            f.setUrl(getUrl(objectSummary.getKey()));
            files.add(f);
        }
        return files;
    }

    private URL getUrl(String objectName) {
        URL url;
        
      java.util.Date expiration = new java.util.Date();
      long msec = expiration.getTime();
      msec += 1000 * 60 * 60 * 24 * 7; // 7 days.
      expiration.setTime(msec);

      GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, objectName);
      generatePresignedUrlRequest.setExpiration(expiration);

      url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return url;
    }

    public void delete(String key) {
        amazonS3.deleteObject(bucket, key);
    }

}
