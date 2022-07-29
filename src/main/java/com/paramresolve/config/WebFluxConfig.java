package com.paramresolve.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@ConditionalOnClass(name = "org.springframework.web.reactive.DispatcherHandler")
@ConditionalOnMissingBean(name = "org.springframework.web.servlet.DispatcherServlet")
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new WebFluxRequestBodyExtractMethodArgumentResolver());
    }
}


