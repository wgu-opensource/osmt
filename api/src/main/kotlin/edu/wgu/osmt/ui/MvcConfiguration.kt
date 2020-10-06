package edu.wgu.osmt.ui

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.*
import java.util.concurrent.TimeUnit


@Configuration
class UiAppConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .addResourceLocations("classpath:/ui/", "classpath:/ui/images/")
    }

    // TODO (julian) Temporarily had to be removed to stop redirecting away from assets
    // The right move here seems to be to set this up to redirect against all non-asset paths only
//    @Override
//    override fun addViewControllers(registry: ViewControllerRegistry) {
//        registry.addViewController("/{notApi:(?!api)[^\\.]*(?!\\.\\w+)}/**").setViewName("forward:/")
//    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}
