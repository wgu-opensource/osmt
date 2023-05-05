package edu.wgu.osmt.interceptor

import edu.wgu.osmt.RoutePaths.SEARCH_COLLECTIONS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter


@Component
class SearchControllerInterceptorAppConfig: WebMvcConfigurerAdapter() {

    @Autowired
    var searchControllerInterceptor: SearchControllerInterceptor? = null

    override fun addInterceptors(registry: InterceptorRegistry) {
        searchControllerInterceptor?.let { registry.addInterceptor(it).addPathPatterns(SEARCH_COLLECTIONS) }
    }

}