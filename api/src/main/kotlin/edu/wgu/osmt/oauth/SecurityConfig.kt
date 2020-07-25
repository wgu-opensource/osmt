package edu.wgu.osmt.oauth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Bean
    fun springSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain{
        http.authorizeExchange()
                .pathMatchers("/rich-skill/**").authenticated()
                .pathMatchers("/", "/rich-skill").permitAll()
                .and().oauth2Login()

        return http.build()
    }

}
