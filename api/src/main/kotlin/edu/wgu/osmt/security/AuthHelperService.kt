package edu.wgu.osmt.security

import edu.wgu.osmt.config.UNAUTHENTICATED_USERNAME
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.db.PublishStatus.Archived
import edu.wgu.osmt.db.PublishStatus.Unarchived
import org.springframework.stereotype.Service
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt


@Service
class AuthHelperService {

    fun readableUsername(user: OAuth2User?, default: String = UNAUTHENTICATED_USERNAME): String {
        return user?.name ?: default
    }

    fun readableUsername(jwt: Jwt?, default: String = UNAUTHENTICATED_USERNAME): String {
        return jwt?.claims?.get("name") as String? ?: default
    }

    fun hasRole(role: String): Boolean {
        val roles = SecurityContextHolder.getContext().authentication.authorities.toString()
        return roles.contains(role);
    }

    fun hasPublishStatus(status: PublishStatus?, statuses: List<PublishStatus>): Boolean {
        return (status == null) || statuses.any { it == status }
    }

    fun isArchiveRelated(status: PublishStatus?): Boolean {
        return hasPublishStatus(status, listOf(Archived, Unarchived))
    }
}
