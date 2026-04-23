package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {
    private String uploadPath = "D:/myproject-uploads";
    private long maxSize = 104857600; // 100MB
    private String[] allowedTypes = {"*"}; // 允许所有类型，生产环境应限制
}
