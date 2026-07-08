package edu.wgu.osmt.security

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Data class for login request body
 */
data class LoginRequest(
    @JsonProperty("username") val username: String,
    @JsonProperty("password") val password: String,
)

/**
 * Data class for login response body
 */
data class LoginResponse(
    val token: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)

/**
 * Controller for admin authentication in single-auth profile.
 *
 * Provides a login endpoint that validates admin credentials and returns JWT tokens
 * for authenticated sessions.
 *
 * **Security Warning**: This controller is only active when the `single-auth` profile is enabled.
 * It should NEVER be used in production environments.
 */
@RestController
@RequestMapping("/api/auth")
@Profile("single-auth & !readonly")
class AdminAuthController
    @Autowired
    constructor(
        private val environment: Environment,
        private val adminTokenService: AdminTokenService,
    ) {
        private val logger = LoggerFactory.getLogger(AdminAuthController::class.java)

        private val adminUsername: String
            get() {
                val username =
                    environment.getProperty("app.single-auth.admin-username")
                        ?: environment.getProperty("app.singleAuth.adminUsername")
                        ?: environment.getProperty("app.single_auth.admin_username")
                        ?: System.getenv("SINGLE_AUTH_ADMIN_USERNAME")
                        ?: System.getenv("APP_SINGLE_AUTH_ADMIN_USERNAME")
                        ?: AdminAuthConstants.DEFAULT_ADMIN_USERNAME

                return username
            }

        private val adminPassword: String
            get() {
                val password =
                    environment.getProperty("app.single-auth.admin-password")
                        ?: environment.getProperty("app.singleAuth.adminPassword")
                        ?: environment.getProperty("app.single_auth.admin_password")
                        ?: System.getenv("SINGLE_AUTH_ADMIN_PASSWORD")
                        ?: System.getenv("APP_SINGLE_AUTH_ADMIN_PASSWORD")
                        ?: AdminAuthConstants.DEFAULT_ADMIN_PASSWORD

                return password
            }

        /**
         * Login endpoint for admin authentication.
         *
         * @param loginRequest The login request containing username and password
         * @return ResponseEntity with JWT token on success, or 401 Unauthorized on failure
         */
        @PostMapping("/login")
        fun login(
            @RequestBody loginRequest: LoginRequest,
        ): ResponseEntity<*> =
            try {
                if (loginRequest.username == adminUsername && loginRequest.password == adminPassword) {
                    val token = adminTokenService.createAdminToken()
                    val response =
                        LoginResponse(
                            token = token,
                            expiresIn = 3600,
                            tokenType = "Bearer",
                        )

                    logger.info("Admin login successful for user: ${loginRequest.username}")
                    ResponseEntity.ok(response)
                } else {
                    logger.warn("Failed login attempt for user: ${loginRequest.username}")
                    ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf("error" to "Invalid credentials"))
                }
            } catch (e: Exception) {
                logger.error("Login error: ${e.javaClass.simpleName} - ${e.message}", e)
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Login failed"))
            }
    }
