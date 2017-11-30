package com.gopivotal.cf.samples.s3.web;

import java.net.URL;
import java.util.Date;

public class S3File {

    private String key;
    private String name;
    private Date date;
    private URL url;

    S3File(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}