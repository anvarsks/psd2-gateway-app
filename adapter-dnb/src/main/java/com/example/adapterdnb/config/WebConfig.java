package com.example.adapterdnb.config;

import com.example.adapterdnb.security.InternalAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InternalAccessInterceptor internalAccessInterceptor;

    public WebConfig(InternalAccessInterceptor internalAccessInterceptor) {
        this.internalAccessInterceptor = internalAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalAccessInterceptor).addPathPatterns("/internal/**");
    }
}
