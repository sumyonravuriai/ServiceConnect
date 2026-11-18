package com.serviceconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve frontend folder dynamically relative to working directory
        File frontendDir = new File("frontend");
        if (!frontendDir.exists()) {
            frontendDir = new File("../frontend");
        }
        String frontendPath = frontendDir.getAbsoluteFile().toURI().toString();

        registry.addResourceHandler("/**")
                .addResourceLocations(frontendPath, "classpath:/static/", "classpath:/public/");
    }
}
