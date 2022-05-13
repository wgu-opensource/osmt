package edu.wgu.osmt.security

import edu.wgu.osmt.db.PublishStatus
import org.springframework.security.core.context.SecurityContextHolder
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

    fun hasRole(role: String): Boolean {
        val roles = SecurityContextHolder.getContext().authentication.authorities.toString()
        return roles.contains(role)
    }

    fun isArchiveRelated(status: PublishStatus?, statuses: List<PublishStatus>): Boolean {
        return (status != null) && statuses.any { it == status }
//        return (status != null) && statuses.contains(status.toString())
    }
}