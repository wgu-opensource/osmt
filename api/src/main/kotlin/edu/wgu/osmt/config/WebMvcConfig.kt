package edu.wgu.osmt.config

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import edu.wgu.osmt.security.AuthHelper
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebMvcConfig : WebMvcConfigurer {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .registerModule(JavaTimeModule())
            .setDateFormat(StdDateFormat())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Autowired
    private lateinit var messageConvertersProvider: ObjectProvider<HttpMessageConverters>

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {
        this.messageConvertersProvider
            .ifAvailable({ customConverters -> converters.addAll(customConverters.getConverters()) })
    }

    @Lazy
    @Bean
    fun getOauth2Helper(): AuthHelper {
        return AuthHelper(SecurityContextHolder.getContext())
    }
}
