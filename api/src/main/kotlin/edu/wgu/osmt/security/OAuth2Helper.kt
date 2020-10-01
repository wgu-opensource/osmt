package edu.wgu.osmt.security

import org.springframework.security.oauth2.core.user.OAuth2User

object OAuth2Helper {
    fun readableUsername(user: OAuth2User?, default: String = "unauthorized"): String {
        return user?.name ?: default
    }
}