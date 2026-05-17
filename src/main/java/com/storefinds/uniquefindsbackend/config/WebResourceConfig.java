package com.storefinds.uniquefindsbackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class WebResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Inject storage properties for local image static resource mapping.
     * Params:
     * - fileStorageProperties: local file storage settings
     * Returns: None
     * Throws: None
     */
    public WebResourceConfig(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Expose uploaded images through a public static resource handler.
     * Params:
     * - registry: mvc resource handler registry
     * Returns: None
     * Throws: None
     */
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path imageDirPath = Paths.get(fileStorageProperties.getImageDir()).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(imageDirPath.toUri().toString());
    }
}
