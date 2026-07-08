package edu.wgu.osmt.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for session invalidation on logout.
 *
 * Invalidates the server-side HTTP session (OAuth2 authorization state).
 * When the frontend clears localStorage and navigates back to login, a fresh
 * OAuth flow can start without stale session state that would cause a broken
 * redirect.
 */
@RestController
@RequestMapping("/api/auth")
class AuthLogoutController {
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        request.getSession(false)?.invalidate()
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}
