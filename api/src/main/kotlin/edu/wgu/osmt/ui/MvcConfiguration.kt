package edu.wgu.osmt.ui

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
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

    /**
     * Add forwarding rules to the UI here
     * - forward non-api routes to UI to handle
     */
    @Override
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{notApi:(?!api)[^\\.]*(?!\\.\\w+)}/**").setViewName("forward:/")
    }
}
