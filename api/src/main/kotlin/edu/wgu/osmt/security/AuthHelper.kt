package edu.wgu.osmt.security

import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.PublishStatus.Archived
import edu.wgu.osmt.db.PublishStatus.Unarchived
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import edu.wgu.osmt.config.UNAUTHENTICATED_USERNAME


interface AuthHelper {
    fun readableUsername(user: OAuth2User?, default: String = UNAUTHENTICATED_USERNAME): String
    fun readableUsername(jwt: Jwt?, default: String = UNAUTHENTICATED_USERNAME): String
    fun hasRole(role: String): Boolean
    fun hasPublishStatus(status: PublishStatus?, statuses: List<PublishStatus>): Boolean
    fun isArchiveRelated(status: PublishStatus?): Boolean
}

class AuthHelperImpl(val securityContext: SecurityContext) : AuthHelper {

    override fun readableUsername(user: OAuth2User?, default: String): String {
        return user?.name ?: default
    }

    override fun readableUsername(jwt: Jwt?, default: String): String {
        return jwt?.claims?.get("name") as String? ?: default
    }

    override fun hasRole(role: String): Boolean {
        val roles = securityContext.authentication.authorities.toString()
        return roles.contains(role);
    }

    override fun hasPublishStatus(status: PublishStatus?, statuses: List<PublishStatus>): Boolean {
        return (status != null) && statuses.any { it == status }
    }

    override fun isArchiveRelated(status: PublishStatus?): Boolean {
        return hasPublishStatus(status, listOf(Archived, Unarchived))
    }
}