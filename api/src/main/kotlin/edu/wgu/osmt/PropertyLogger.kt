package edu.wgu.osmt

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.PropertySource
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport


@Component
@Profile("dev")
@Order(1)
class PropertyLogger {
    @EventListener
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        val env = event.applicationContext.environment
        LOGGER.info("====== Environment and configuration ======")
        LOGGER.info("Active profiles: {}", Arrays.toString(env.activeProfiles))
        val sources = (env as AbstractEnvironment).propertySources
        val unsecretProperties: Stream<String> = StreamSupport.stream(sources.spliterator(), false)
            .filter { ps: PropertySource<*>? -> ps is EnumerablePropertySource<*> }
            .map { ps: PropertySource<*> -> (ps as EnumerablePropertySource<*>).propertyNames }
            .flatMap { array: Array<String>? -> Arrays.stream(array) }
            .distinct()
            .filter { prop: String -> !(
                    prop.contains("credentials", true) ||
                            prop.contains("secret", true) ||
                            prop.contains("password", true)
                    )}

        unsecretProperties.sorted().collect(Collectors.toList())
            .forEach { prop: String? -> LOGGER.info("{}: {}", prop, env.getProperty(prop!!)) }

        LOGGER.info("===========================================")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PropertyLogger::class.java)
    }
}