package edu.wgu.osmt.oauth

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

//TODO not sure why this is needed
@Component
class AuthenticationManager : ReactiveAuthenticationManager{

    @Override
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
    }
}
