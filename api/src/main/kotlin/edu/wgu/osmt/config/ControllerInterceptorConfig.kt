package edu.wgu.osmt.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Component
class ControllerInterceptorConfig : WebMvcConfigurerAdapter() {

    @Autowired
    var controllerInterceptor: ControllerInterceptor? = null

    override fun addInterceptors(registry: InterceptorRegistry) {
        controllerInterceptor?.let { registry.addInterceptor(it) }
    }

}
