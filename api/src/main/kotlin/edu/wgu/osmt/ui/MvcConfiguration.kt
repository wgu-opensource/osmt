package edu.wgu.osmt.ui

import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.*
import java.util.concurrent.TimeUnit


@Configuration
class UiAppConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/*")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .addResourceLocations("classpath:/ui/")
        registry.addResourceHandler("/assets/**")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .addResourceLocations("classpath:/ui/assets/")
    }

    /**
     * Redirect all sub-paths that are not api or static asset related back to the the root
     */
    @Override
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{notApiOrAssetPaths:(?!api|assets)[^\\.]*(?!\\.\\w+)}/**").setViewName("forward:/")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}
