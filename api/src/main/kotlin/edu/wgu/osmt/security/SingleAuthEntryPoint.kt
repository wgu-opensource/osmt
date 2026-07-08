package edu.wgu.osmt.security

import com.fasterxml.jackson.databind.ObjectMapper
import edu.wgu.osmt.api.model.ApiError
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * Custom authentication entry point for single-auth profile.
 *
 * This entry point returns a JSON 401 response without WWW-Authenticate headers
 * to prevent the browser's Basic Auth dialog from appearing for UI routes.
 *
 * **Security Warning**: This component is only active when the `single-auth` profile is enabled.
 */
@Component
@Profile("single-auth & !readonly")
class SingleAuthEntryPoint
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
    ) : AuthenticationEntryPoint {
        override fun commence(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            authException: AuthenticationException?,
        ) {
            response?.let {
                it.status = HttpServletResponse.SC_UNAUTHORIZED
                it.contentType = "application/json"
                it.characterEncoding = "UTF-8"
                // Explicitly don't set WWW-Authenticate header to prevent browser dialog
                val apiError = ApiError("Unauthorized")
                objectMapper.writeValue(it.writer, apiError)
                it.flushBuffer()
            }
        }
    }
