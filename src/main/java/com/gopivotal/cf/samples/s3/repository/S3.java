package com.gopivotal.cf.samples.s3.repository;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.gopivotal.cf.samples.s3.connector.cloudfoundry.S3ServiceInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3 {

    Log log = LogFactory.getLog(S3.class);

    private AmazonS3 amazonS3;
    private String bucket;
    private String baseUrl;
    private Boolean usePresignedUrls;

    public S3(S3ServiceInfo serviceInfo) {

        AWSCredentials awsCredentials = new BasicAWSCredentials(serviceInfo.getAccessKey(), serviceInfo.getSecretKey());

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withPathStyleAccessEnabled(serviceInfo.getPathStyleAccess());

        if (serviceInfo.getEndpoint() != null) {
            // if a custom endpoint is set, we will ignore the region
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    serviceInfo.getEndpoint(), "Standard"
            ));
        } else {
            builder.withRegion(serviceInfo.getRegion());
        }

        AmazonS3 amazonS3 = builder.build();

        try {
            amazonS3.createBucket(
                    new CreateBucketRequest(serviceInfo.getBucket()).withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (AmazonServiceException e) {
            if (!e.getErrorCode().equals("BucketAlreadyOwnedByYou")) {
                throw e;
            }
        }
        log.info("Using S3 Bucket: " + serviceInfo.getBucket());

        this.amazonS3 = amazonS3;
        this.bucket = serviceInfo.getBucket();
        this.baseUrl = serviceInfo.getBaseUrl();
        this.usePresignedUrls = serviceInfo.getUsePresignedUrls();

        Pattern r = Pattern.compile("^http(s)*://(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])");
        Matcher m = r.matcher(serviceInfo.getEndpoint());
        if (m.find( )) {
            log.info("Endpoint host is an IP, using presigned URLs");
            this.usePresignedUrls = true;
        }
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
        
        if (usePresignedUrls) {
          java.util.Date expiration = new java.util.Date();
          long msec = expiration.getTime();
          msec += 1000 * 60 * 60 * 24 * 7; // 7 days.
          expiration.setTime(msec);

          GeneratePresignedUrlRequest generatePresignedUrlRequest =
                        new GeneratePresignedUrlRequest(bucket, objectName);
          generatePresignedUrlRequest.setExpiration(expiration);

          url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        } else {
          if (baseUrl == null) {
              url = amazonS3.getUrl(bucket, objectName);
          } else {
              try {
                  url = new URL(baseUrl + "/" + bucket + "/" + objectName);
              } catch (MalformedURLException e) {
                  throw new RuntimeException("Error generating URL");
              }
          }
        }

        return url;
    }

    public void delete(String key) {
        amazonS3.deleteObject(bucket, key);
    }

}
