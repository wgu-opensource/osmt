package edu.wgu.osmt.ui

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit


@Configuration
class UiAppConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .addResourceLocations("classpath:/ui/")
    }

    @Override
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{notApi:(?!api)[^\\.]*(?!\\.\\w+)}/**").setViewName("forward:/")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}
