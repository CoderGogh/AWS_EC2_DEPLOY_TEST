package com.mycom.myapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 1. ALB 전용 Health Check (우리가 설정한 /index.html 대신 이것도 가능)
    @GetMapping("/health")
    public String healthCheck() {
        return "I am Healthy!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, AWS CI/CD!";
    }

    // 2. S3 이미지 업로드 (images 패키지/폴더 안으로 전송)
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = "images/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // 3. S3 이미지 수신 (파일명으로 URL 조회)
    @GetMapping("/image/{fileName}")
    public String getImageUrl(@PathVariable String fileName) {
        return amazonS3.getUrl(bucket, "images/" + fileName).toString();
    }

}
