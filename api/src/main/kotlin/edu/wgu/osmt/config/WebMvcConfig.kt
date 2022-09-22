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
import org.springframework.lang.NonNull
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


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

@Bean
fun webMvcConfigurer(concurrentTaskExecutor: ConcurrentTaskExecutor?): WebMvcConfigurer? {
    return object : WebMvcConfigurer {
        override fun configureAsyncSupport(@NonNull configurer: AsyncSupportConfigurer) {
            configurer.setDefaultTimeout(-1)
            configurer.setTaskExecutor(concurrentTaskExecutor!!)
        }
    }
}

@Bean
fun concurrentTaskExecutor(): ConcurrentTaskExecutor? {
    return ConcurrentTaskExecutor(Executors.newFixedThreadPool(5, object : ThreadFactory {
        private val threadCounter = AtomicInteger(0)
        override fun newThread(@NonNull runnable: Runnable): Thread {
            return Thread(runnable, "asyncThread-" + threadCounter.incrementAndGet())
        }
    }))
}
