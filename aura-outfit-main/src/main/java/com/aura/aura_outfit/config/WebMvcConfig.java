package com.aura.aura_outfit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS, JS e imagens: cache longo (1 ano)
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/icons/**", "/fonts/**")
                .addResourceLocations(
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/images/",
                        "classpath:/static/icons/",
                        "classpath:/static/fonts/"
                )
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());

        // HTML e demais recursos: sem cache (sempre busca versão nova)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());
    }
}
