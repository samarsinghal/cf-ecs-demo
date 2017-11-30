package com.gopivotal.cf.samples.s3.web;

import com.amazonaws.services.s3.model.*;
import com.emc.ecs.connector.spring.S3Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class S3Controller {

    Log log = LogFactory.getLog(S3Controller.class);

    @Autowired
    private S3Connector s3;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("message", "Hello Boot!");
        Iterable<S3File> images = list();
        model.addAttribute("images", images);
        return "index";
    }

    @RequestMapping(value = "/delete/{key:.+}", method = RequestMethod.GET)
    public String deleteFile(@PathVariable String key) {
        delete(key);
        log.info(String.format("Image '%s' deleted from S3 bucket.", key));
        return "redirect:/";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        File uploadedFile = new File(file.getOriginalFilename());

        try {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(uploadedFile));
            stream.write(bytes);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file.", e);
        }

        try {
            put(file.getOriginalFilename(), uploadedFile);
            log.info(file.getOriginalFilename() + " put to S3.");
        } catch (MalformedURLException e){
            throw new RuntimeException("Failed saving file to backend.", e);
        }

        uploadedFile.delete();
        log.info(uploadedFile.getAbsolutePath() + " temporary file is deleted.");

        return "redirect:/";
    }

    private void put(String filename, File content) throws MalformedURLException {
        String key = String.format("%s-%s", UUID.randomUUID().toString(), filename);
        PutObjectRequest putObjectRequest = new PutObjectRequest(s3.getBucket(), key, content)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        s3.getClient().putObject(putObjectRequest);
    }

    private Iterable<S3File> list() {
        Pattern p = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}-(.*)");
        ObjectListing listing = s3.getClient().listObjects(s3.getBucket());
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
                new GeneratePresignedUrlRequest(s3.getBucket(), objectName);
        generatePresignedUrlRequest.setExpiration(expiration);

        url = s3.getClient().generatePresignedUrl(generatePresignedUrlRequest);

        return url;
    }

    private void delete(String key) {
        s3.getClient().deleteObject(s3.getBucket(), key);
    }

}
