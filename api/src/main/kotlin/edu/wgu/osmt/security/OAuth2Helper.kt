package edu.wgu.osmt.security

import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt

object OAuth2Helper {
    val UNAUTHENTICATED_USERNAME = "unauthenticated"

    fun readableUsername(user: OAuth2User?, default: String = UNAUTHENTICATED_USERNAME): String {
        return user?.name ?: default
    }

    fun readableUsername(jwt: Jwt?, default: String = UNAUTHENTICATED_USERNAME): String {
        return jwt?.claims?.get("name") as String? ?: default
    }
}