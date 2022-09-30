package edu.wgu.osmt.config

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.util.concurrent.Executor
import java.util.concurrent.Executors


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
}

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    @Bean
    protected fun webMvcConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurerAdapter() {
            override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
                configurer.setTaskExecutor(taskExecutor)
            }
        }
    }

    @get:Bean
    protected val taskExecutor: ConcurrentTaskExecutor
        protected get() {
            // TODO - make this value runtime configurable with sensible default
            val threadCount = 5
            return ConcurrentTaskExecutor(Executors.newFixedThreadPool(threadCount))
        }
}
