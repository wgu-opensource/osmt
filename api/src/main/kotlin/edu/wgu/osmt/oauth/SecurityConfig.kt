package edu.wgu.osmt.oauth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Override
    fun configure(http: HttpSecurity) {
        http.authorizeRequests().antMatchers("/skills/insert-random", "/jobcode/**").authenticated()
            .antMatchers("/", "/skills", "jobcode", "/enqueue").permitAll()
            .and().oauth2Login()
    }

}
