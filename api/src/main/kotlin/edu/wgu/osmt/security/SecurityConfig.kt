package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var successHandler: OurRedirectHandler

    @Override
    override fun configure(http: HttpSecurity) {
        http.cors().disable()
            .csrf().disable()
            .httpBasic().disable()
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .and()
            .oauth2Login()
            .successHandler(successHandler)
    }

}


@Component
class OurRedirectHandler : AuthenticationSuccessHandler {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var clientService: OAuth2AuthorizedClientService

    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val redirectStrategy: DefaultRedirectStrategy = DefaultRedirectStrategy()
        when (authentication) {
            is OAuth2AuthenticationToken -> {
                val client: OAuth2AuthorizedClient = clientService.loadAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
                val tokenValue = client.accessToken.tokenValue
                val url = "${appConfig.loginSuccessRedirectUrl}?token=${tokenValue}"
                redirectStrategy.sendRedirect(request, response, url)
            }
        }
    }
}