package com.dak.spravel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for Vue SPA assets and Scalar UI routing.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Vue SPA static assets di bawah /_/
        // assets/ path sudah termasuk /_/ dari Vite base
        registry.addResourceHandler("/_/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        // Serve Scalar UI static assets
        registry.addResourceHandler("/_/static/scalar-ui/**")
                .addResourceLocations("classpath:/static/scalar-ui/")
                .setCachePeriod(31556926)
                .resourceChain(true);

        // Favicon dan file root di static/
        registry.addResourceHandler("/*.svg", "/*.ico", "/*.png")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Redirect root / ke /_/ agar langsung masuk Vue SPA.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/_/");
    }
}
