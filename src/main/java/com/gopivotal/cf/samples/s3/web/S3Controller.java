package com.gopivotal.cf.samples.s3.web;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gopivotal.cf.samples.s3.repository.S3;
import com.gopivotal.cf.samples.s3.repository.S3File;
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
import java.util.List;
import java.util.UUID;

@Controller
public class S3Controller {

    Log log = LogFactory.getLog(S3Controller.class);

    @Autowired
    S3 s3;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("message", "Hello Boot!");
        Iterable<S3File> images = s3.list();
        model.addAttribute("images", images);
        return "index";
    }

    @RequestMapping(value = "/delete/{key:.+}", method = RequestMethod.GET)
    public String deleteFile(@PathVariable String key) {
        s3.delete(key);
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
            s3.put(file.getOriginalFilename(), uploadedFile);
            log.info(file.getOriginalFilename() + " put to S3.");
        } catch (MalformedURLException e){
            throw new RuntimeException("Failed saving file to backend.", e);
        }

        uploadedFile.delete();
        log.info(uploadedFile.getAbsolutePath() + " temporary file is deleted.");

        return "redirect:/";
    }
}
