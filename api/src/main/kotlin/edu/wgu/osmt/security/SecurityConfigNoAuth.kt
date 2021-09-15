package edu.wgu.osmt.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@Profile("noauth")
@Order(50)
class SecurityConfigNoAuth : WebSecurityConfigurerAdapter() {

    @Override
    override fun configure(http: HttpSecurity) {
        http
            .cors().and()
            .csrf().disable()
            .httpBasic().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET,  "/api/skills").permitAll()
            .antMatchers(HttpMethod.POST, "/api/skills").permitAll()
            .antMatchers(HttpMethod.POST, "/api/skills/*/update").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/collections").permitAll()
            .antMatchers(HttpMethod.POST,  "/api/collections").permitAll()
            .antMatchers(HttpMethod.POST,  "/api/collections/*/update").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/collections/*").permitAll()
            //public endpoints
            .antMatchers(HttpMethod.GET,  "/api/skills/*").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/collections/*").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/collections/*/skills").permitAll()
            .antMatchers(HttpMethod.GET,  "/api/tasks/*").permitAll()
            .antMatchers("/**").permitAll()
    }
}