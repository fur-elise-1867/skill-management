package com.furelise.skillmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC configuration for serving static uploads (e.g. avatars).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/avatars/}")
    private String uploadDirPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + path.toString() + "/");
    }
}
