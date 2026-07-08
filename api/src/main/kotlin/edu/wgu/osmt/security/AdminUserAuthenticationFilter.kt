package edu.wgu.osmt.security

import edu.wgu.osmt.config.AppConfig
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant

/**
 * Constants for admin authentication.
 */
object AdminAuthConstants {
    /** Default admin username for development */
    const val DEFAULT_ADMIN_USERNAME = "admin"

    /** Default admin password for development */
    const val DEFAULT_ADMIN_PASSWORD = "admin"

    /** Admin role granted to authenticated users */
    const val ADMIN_ROLE = "ROLE_Osmt_Admin"

    /** Admin email for JWT claims */
    const val ADMIN_EMAIL = "admin@localhost"

    /** Admin name for JWT claims */
    const val ADMIN_NAME = "Administrator"
}

/**
 * Authentication filter that validates admin credentials for the single-auth profile.
 *
 * This filter extracts admin authentication information from the Authorization header
 * (Basic Auth or Bearer token) and validates it against configured admin credentials.
 * On successful authentication, it creates a JWT token with admin role and sets it
 * in the Spring Security context.
 *
 * **Security Warning**: This filter is only active when the `single-auth` profile is enabled.
 * It should NEVER be used in production environments.
 *
 * Authentication supports:
 * - HTTP Basic Authentication (username:password base64 encoded)
 * - Bearer token authentication (JWT tokens issued by login endpoint)
 *
 * @see SingleAuthSecurityConfig for the security configuration that uses this filter
 * @see AdminAuthController for the login endpoint that issues JWT tokens
 */
@Component
@Profile("single-auth & !readonly")
@Order(1)
class AdminUserAuthenticationFilter
    @Autowired
    constructor(
        private val appConfig: AppConfig,
        private val environment: Environment,
        @Qualifier("adminTokenJwtDecoder") private val adminTokenJwtDecoder: JwtDecoder,
    ) : OncePerRequestFilter() {
        private val logger = LoggerFactory.getLogger(AdminUserAuthenticationFilter::class.java)

        private val adminUsername: String
            get() {
                return environment.getProperty("app.single-auth.admin-username")
                    ?: environment.getProperty("app.singleAuth.adminUsername")
                    ?: environment.getProperty("app.single_auth.admin_username")
                    ?: System.getenv("SINGLE_AUTH_ADMIN_USERNAME")
                    ?: System.getenv("APP_SINGLE_AUTH_ADMIN_USERNAME")
                    ?: AdminAuthConstants.DEFAULT_ADMIN_USERNAME
            }

        private val adminPassword: String
            get() {
                return environment.getProperty("app.single-auth.admin-password")
                    ?: environment.getProperty("app.singleAuth.adminPassword")
                    ?: environment.getProperty("app.single_auth.admin_password")
                    ?: System.getenv("SINGLE_AUTH_ADMIN_PASSWORD")
                    ?: System.getenv("APP_SINGLE_AUTH_ADMIN_PASSWORD")
                    ?: AdminAuthConstants.DEFAULT_ADMIN_PASSWORD
            }

        /**
         * Processes the request to validate admin authentication.
         *
         * This method:
         * 1. Checks for Authorization header
         * 2. Validates Basic Auth credentials or Bearer token
         * 3. Creates a JWT token with admin claims and role
         * 4. Sets the authentication in SecurityContextHolder
         * 5. Continues the filter chain
         *
         * If no valid authentication is found, the request continues without authentication
         * (allowing the security configuration to handle unauthorized access appropriately).
         *
         * @param request HTTP servlet request
         * @param response HTTP servlet response
         * @param filterChain Filter chain to continue processing
         */
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
        ) {
            val authHeader = request.getHeader("Authorization")

            if (authHeader != null) {
                try {
                    val authentication =
                        when {
                            authHeader.startsWith("Basic ") -> validateBasicAuth(authHeader)
                            authHeader.startsWith("Bearer ") -> validateBearerToken(authHeader)
                            else -> null
                        }

                    if (authentication != null) {
                        SecurityContextHolder.getContext().authentication = authentication
                        logger.debug("Admin authentication successful")
                    }
                } catch (e: Exception) {
                    logger.warn("Authentication validation failed: ${e.message}")
                }
            }

            filterChain.doFilter(request, response)
        }

        /**
         * Validates HTTP Basic Authentication credentials.
         *
         * @param authHeader The Authorization header value (Basic base64-encoded-credentials)
         * @return JwtAuthenticationToken if valid, null otherwise
         */
        private fun validateBasicAuth(authHeader: String): JwtAuthenticationToken? =
            try {
                val base64Credentials = authHeader.substringAfter("Basic ").trim()
                val credentials =
                    String(
                        java.util.Base64
                            .getDecoder()
                            .decode(base64Credentials),
                    )
                val (username, password) = credentials.split(":", limit = 2)

                if (username == adminUsername && password == adminPassword) {
                    createAdminJwtAuthentication()
                } else {
                    logger.warn("Invalid admin credentials provided for user: $username")
                    null
                }
            } catch (e: Exception) {
                logger.warn("Failed to parse Basic Auth credentials: ${e.javaClass.simpleName}")
                null
            }

        /**
         * Validates Bearer token authentication.
         *
         * Decodes and validates the JWT signature and expiration.
         * Only accepts tokens issued by the admin login endpoint.
         *
         * @param authHeader The Authorization header value (Bearer token)
         * @return JwtAuthenticationToken if valid, null otherwise
         */
        private fun validateBearerToken(authHeader: String): JwtAuthenticationToken? =
            try {
                val token = authHeader.substringAfter("Bearer ").trim()
                if (token.isEmpty()) {
                    null
                } else {
                    val jwt = adminTokenJwtDecoder.decode(token)
                    val authorities = authoritiesFromJwt(jwt)
                    JwtAuthenticationToken(jwt, authorities)
                }
            } catch (e: JwtException) {
                logger.debug("Invalid Bearer token: ${e.message}")
                null
            } catch (e: Exception) {
                logger.warn("Failed to validate Bearer token: ${e.message}")
                null
            }

        private fun authoritiesFromJwt(jwt: Jwt): List<SimpleGrantedAuthority> {
            val rolesClaim = jwt.claims["roles"]
            return when (rolesClaim) {
                is String -> {
                    listOf(SimpleGrantedAuthority(rolesClaim))
                }

                is Collection<*> -> {
                    rolesClaim
                        .filterIsInstance<String>()
                        .map { SimpleGrantedAuthority(it) }
                }

                else -> {
                    listOf(SimpleGrantedAuthority(AdminAuthConstants.ADMIN_ROLE))
                }
            }
        }

        /**
         * Creates a JWT authentication token with admin role for Basic Auth.
         *
         * Used when Basic Auth credentials are valid. The in-memory Jwt
         * is not validated (Basic Auth was already checked).
         *
         * @return JwtAuthenticationToken with admin authorities
         */
        private fun createAdminJwtAuthentication(): JwtAuthenticationToken {
            val authorities = listOf(SimpleGrantedAuthority(AdminAuthConstants.ADMIN_ROLE))

            val jwt =
                Jwt
                    .withTokenValue("basic-auth-admin")
                    .header("typ", "JWT")
                    .header("alg", "none")
                    .claim("email", AdminAuthConstants.ADMIN_EMAIL)
                    .claim("name", AdminAuthConstants.ADMIN_NAME)
                    .claim("sub", AdminAuthConstants.ADMIN_EMAIL)
                    .claim("roles", AdminAuthConstants.ADMIN_ROLE)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build()

            return JwtAuthenticationToken(jwt, authorities)
        }
    }
