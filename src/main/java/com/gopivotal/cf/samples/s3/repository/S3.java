package com.gopivotal.cf.samples.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

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
    private String baseUrl;

    public S3(AmazonS3 amazonS3, String bucket, String baseUrl) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.baseUrl = baseUrl;
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
        if (baseUrl == null) {
            url = amazonS3.getUrl(bucket, objectName);
        } else {
            try {
                url = new URL(baseUrl + "/" + bucket + "/" + objectName);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error generating URL");
            }
        }
        return url;
    }

    public void delete(String key) {
        amazonS3.deleteObject(bucket, key);
    }

}