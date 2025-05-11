package com.barisdalyanemre.librarymanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.File;
import java.io.IOException;

/**
 * Configuration class for loading environment variables from .env file
 * Used primarily in production profile
 */
@Configuration
@Slf4j
public class EnvConfig {

    @Bean
    @Profile("production")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        
        // Look for .env file in the project root
        String rootPath = System.getProperty("user.dir");
        File envFile = new File(rootPath + "/../.env");
        log.info("Looking for .env file at: {}", envFile.getAbsolutePath());
        
        if (envFile.exists()) {
            configurer.setLocation(new FileSystemResource(envFile));
            configurer.setIgnoreResourceNotFound(false);
            configurer.setIgnoreUnresolvablePlaceholders(false);
            log.info(".env file found and will be used");
        } else {
            // Fallback to classpath if .env file is not found
            log.warn(".env file not found at {}. Will try to use environment variables directly.", envFile.getAbsolutePath());
            configurer.setIgnoreResourceNotFound(true);
        }
        
        return configurer;
    }
    
    @Bean
    @Profile("production")
    public static PropertySourcesPlaceholderConfigurer validateRequiredProperties(Environment env) {
        PropertySourcesPlaceholderConfigurer configurer = propertySourcesPlaceholderConfigurer();
        
        // Validate that required environment variables are set
        String[] requiredProps = {
            "POSTGRES_URL", 
            "POSTGRES_USER", 
            "POSTGRES_PASSWORD",
            "ADMIN_FIRST_NAME",
            "ADMIN_LAST_NAME",
            "ADMIN_PASSWORD",
            "ADMIN_EMAIL"
        };
        
        for (String prop : requiredProps) {
            if (env.getProperty(prop) == null) {
                log.error("Required environment variable {} is not set", prop);
            } else {
                log.debug("Environment variable {} is set", prop);
            }
        }
        
        return configurer;
    }
    
    @Bean
    @Profile("development")
    public static PropertySourcesPlaceholderConfigurer devPropertiesConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        
        try {
            // Try to load .env file for development if exists
            String rootPath = System.getProperty("user.dir");
            File envFile = new File(rootPath + "/../.env");
            
            if (envFile.exists()) {
                log.info("Loading .env file for development profile: {}", envFile.getAbsolutePath());
                Resource resource = new FileSystemResource(envFile);
                configurer.setLocation(resource);
                
                // Also add as property source for more flexible access
                try {
                    ResourcePropertySource propertySource = new ResourcePropertySource(resource);
                    log.info("Successfully loaded {} properties from .env file", propertySource.getPropertyNames().length);
                } catch (IOException e) {
                    log.warn("Failed to load .env file as property source", e);
                }
            }
        } catch (Exception e) {
            log.warn("Error loading .env file for development", e);
        }
        
        return configurer;
    }
}
