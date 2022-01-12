package edu.wgu.osmt.ui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File
import java.util.concurrent.TimeUnit


@Configuration
class UiAppConfig : WebMvcConfigurer {
    val log: Logger = LoggerFactory.getLogger(UiAppConfig::class.java)

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/*")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .addResourceLocations("classpath:/ui/")
        registry.addResourceHandler("/assets/**")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .addResourceLocations("classpath:/ui/assets/")

        // Optional white label
        val whitelabelPath = System.getenv("WHITELABEL_PATH")
        if (!whitelabelPath.isNullOrBlank()) {
            whitelabelPath.takeIf { File(it).isDirectory }
                ?.let {
                    registry.addResourceHandler("whitelabel/**")
                        .setCacheControl(CacheControl.noCache())
                        .addResourceLocations("file:${it}/")
                } ?: log.warn("No whitelabel environment file defined")
        }
    }

    /**
     * Redirect all sub-paths that are not api or static asset related back to the the root
     */
    @Override
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{notApiOrAssetPaths:(?!api|assets|whitelabel)[^\\.]*(?!\\.\\w+)}/**").setViewName("forward:/")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}
