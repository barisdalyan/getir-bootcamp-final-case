package com.barisdalyanemre.librarymanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configuration class to enable WebFlux features while coexisting with Spring MVC
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
}
