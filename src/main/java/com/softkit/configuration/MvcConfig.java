package com.softkit.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    @Value("${images.path.string}")
    private String uploadPath;
    @Value("${file.csv.path}")
    private String pathToCsvFile;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/users/images/**")
                .addResourceLocations("file://" + uploadPath + "/");
        registry.addResourceHandler("/users/exportToCsv/**")
                .addResourceLocations("file://" + pathToCsvFile + "/");
    }
}
