package edu.wgu.osmt.security

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.config.AppConfig
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity

/**
 * Helper utility for shared security configuration between OAuth2 and single-auth profiles.
 *
 * This class provides a single source of truth for all authorization rules, eliminating duplication
 * between [SecurityConfig] and [SingleAuthSecurityConfig].
 *
 * All authorization rules are centralized here to ensure consistency between OAuth2 and single-auth
 * security profiles.
 */
object SecurityConfigHelper {
    /**
     * Builds API path for all versions (v2, v3, unversioned).
     *
     * @param endpoint The endpoint path (e.g., RoutePaths.SKILL_AUDIT_LOG)
     * @return Array of three paths: [v3 path, v2 path, unversioned path]
     */
    fun buildAllVersions(endpoint: String): Array<String> =
        arrayOf(
            RoutePaths.API + RoutePaths.API_V3 + endpoint,
            RoutePaths.API + RoutePaths.API_V2 + endpoint,
            RoutePaths.API + RoutePaths.UNVERSIONED + endpoint,
        )

    /**
     * Configures public endpoints that require no authentication.
     *
     * These endpoints are accessible without authentication:
     * - Search endpoints (POST /api/{version}/search/skills, /api/{version}/search/collections)
     * - Canonical URL endpoints (GET /api/{version}/skills/{uuid},
     * /api/{version}/collections/{uuid})
     * - Collection skills endpoint (POST /api/{version}/collections/{uuid}/skills)
     * - Collection export endpoints (CSV, XLSX)
     * - Custom list export (POST /api/v3/export/skills/csv and .../xlsx) when
     *   controller allows anonymous via allowPublicSearching
     * - Task detail endpoints (text, media)
     *
     * @param http HttpSecurity builder to configure
     * @return HttpSecurity builder for method chaining
     */
    fun configurePublicEndpoints(http: HttpSecurity): HttpSecurity =
        http.authorizeHttpRequests { auth ->
            auth
                // UI routes - must be public to allow login page and static resources
                .requestMatchers("/", "/login", "/login/**", "/login/success")
                .permitAll()
                .requestMatchers(
                    "/assets/**",
                    "/config/**",
                    "/*.js",
                    "/*.css",
                    "/*.html",
                    "/*.ico",
                    "/*.png",
                    "/*.svg",
                ).permitAll()
                // Version endpoint (deployment verification - any method for simplicity)
                .requestMatchers("/version")
                .permitAll()
                // Whitelabel config endpoint (needed for frontend initialization)
                .requestMatchers(GET, "/whitelabel/**")
                .permitAll()
                // Login endpoint for single-auth profile
                .requestMatchers(POST, "/api/auth/login")
                .permitAll()
                // Logout endpoint - invalidates server session (OAuth2 state)
                .requestMatchers(POST, "/api/auth/logout")
                .permitAll()
                // Public search endpoints
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SEARCH_SKILLS))
                .permitAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SEARCH_COLLECTIONS))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SEARCH_KEYWORDS_PATH))
                .permitAll()
                // Public skills filter endpoint (for the skills list page)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILLS_FILTER))
                .permitAll()
                // Public canonical URL endpoints
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILL_DETAIL))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTION_DETAIL))
                .permitAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_SKILLS))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTION_CSV))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTION_XLSX))
                .permitAll()
                .requestMatchers(
                    POST,
                    RoutePaths.API + RoutePaths.API_V3 + RoutePaths.EXPORT_SKILLS_CSV,
                ).permitAll()
                .requestMatchers(
                    POST,
                    RoutePaths.API + RoutePaths.API_V3 + RoutePaths.EXPORT_SKILLS_XLSX,
                ).permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_TEXT))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_MEDIA))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SEARCH_JOBCODES_PATH))
                .permitAll()
                // Public category endpoints (list, detail, skills)
                .requestMatchers(GET, *buildAllVersions(RoutePaths.CATEGORY_LIST))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.CATEGORY_DETAIL))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.CATEGORY_SKILLS))
                .permitAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.CATEGORY_SKILLS))
                .permitAll()
        }

    /**
     * Configures endpoints that require authentication (but no specific roles).
     *
     * These endpoints require any authenticated user:
     * - Audit log endpoints
     * - Task detail endpoints (skills, batch)
     * - Search metadata endpoints (jobcodes, keywords)
     *
     * @param http HttpSecurity builder to configure
     * @return HttpSecurity builder for method chaining
     */
    fun configureAuthenticatedEndpoints(http: HttpSecurity): HttpSecurity =
        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILL_AUDIT_LOG))
                .authenticated()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTION_AUDIT_LOG))
                .authenticated()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_SKILLS))
                .authenticated()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_BATCH))
                .authenticated()
        }

    /**
     * Configures list endpoints with role-based or public access.
     *
     * @param http HttpSecurity builder to configure
     * @param allowPublic If true, lists are public; if false, require ADMIN, CURATOR, VIEW, or READ
     * roles
     * @param admin Admin role name
     * @param curator Curator role name
     * @param view View role name
     * @param read Read scope name
     * @return HttpSecurity builder for method chaining
     */
    fun configureListEndpoints(
        http: HttpSecurity,
        allowPublic: Boolean,
        admin: String,
        curator: String,
        view: String,
        read: String,
    ): HttpSecurity =
        http.authorizeHttpRequests { auth ->
            if (allowPublic) {
                auth
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILLS_LIST))
                    .permitAll()
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTIONS_LIST))
                    .permitAll()
            } else {
                auth
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILLS_LIST))
                    .hasAnyAuthority(admin, curator, view, read)
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTIONS_LIST))
                    .hasAnyAuthority(admin, curator, view, read)
            }
        }

    /**
     * Configures role-based endpoints for write operations.
     *
     * Role requirements:
     * - Skill update/create: ADMIN or CURATOR
     * - Skill publish: ADMIN only
     * - Collection create/update: ADMIN or CURATOR
     * - Collection publish: ADMIN only
     * - Collection delete: ADMIN only
     * - Workspace: ADMIN or CURATOR
     * - All other API endpoints: ADMIN, CURATOR, VIEW, or READ
     *
     * @param http HttpSecurity builder to configure
     * @param admin Admin role name
     * @param curator Curator role name
     * @param view View role name
     * @param read Read scope name
     * @return HttpSecurity builder for method chaining
     */
    fun configureRoleBasedEndpoints(
        http: HttpSecurity,
        admin: String,
        curator: String,
        view: String,
        read: String,
    ): HttpSecurity =
        http.authorizeHttpRequests { auth ->
            auth
                // Skill operations
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_UPDATE))
                .hasAnyAuthority(admin, curator)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILLS_CREATE))
                .hasAnyAuthority(admin, curator)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_PUBLISH))
                .hasAnyAuthority(admin)
                // Collection operations
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_CREATE))
                .hasAnyAuthority(admin, curator)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_PUBLISH))
                .hasAnyAuthority(admin)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_UPDATE))
                .hasAnyAuthority(admin, curator)
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_SKILLS_UPDATE))
                .hasAnyAuthority(admin, curator)
                .requestMatchers(DELETE, *buildAllVersions(RoutePaths.COLLECTION_REMOVE))
                .hasAnyAuthority(admin)
                // Workspace
                .requestMatchers(GET, *buildAllVersions(RoutePaths.WORKSPACE_PATH))
                .hasAnyAuthority(admin, curator)
                // Sync single-record endpoints: admin or curator
                .requestMatchers(POST, "/api/sync/skill/**", "/api/sync/collection/**")
                .hasAnyAuthority(admin, curator)
                // Sync bulk endpoints: admin-only (GET state, POST trigger/resync)
                .requestMatchers("/api/sync/**")
                .hasAnyAuthority(admin)
                // Catch-all for other API endpoints
                .requestMatchers("/api/**")
                .hasAnyAuthority(admin, curator, view, read)
                .requestMatchers("/**")
                .permitAll()
        }

    /**
     * Configures endpoints for no-role mode (simple authentication only).
     *
     * In this mode:
     * - List endpoints: Public
     * - Write operations: Require authentication (any authenticated user)
     * - Collection delete: Denied (denyAll)
     * - All other endpoints: Public
     *
     * @param http HttpSecurity builder to configure
     * @return HttpSecurity builder for method chaining
     */
    fun configureNoRoleEndpoints(http: HttpSecurity): HttpSecurity =
        http.authorizeHttpRequests { auth ->
            auth
                // Lists are public
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILLS_LIST))
                .permitAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTIONS_LIST))
                .permitAll()
                // Write operations require authentication
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_UPDATE))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILLS_CREATE))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_PUBLISH))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_CREATE))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_PUBLISH))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_UPDATE))
                .authenticated()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_SKILLS_UPDATE))
                .authenticated()
                // Collection delete is denied
                .requestMatchers(DELETE, *buildAllVersions(RoutePaths.COLLECTION_REMOVE))
                .denyAll()
                // Sync endpoints: require authentication (ensureAdmin in controller)
                .requestMatchers("/api/sync/**")
                .authenticated()
                // Fall-through: all other endpoints are public
                .requestMatchers("/**")
                .permitAll()
        }

    /**
     * Read-only public instance: same public routes as [configurePublicEndpoints], deny
     * authenticated-only reads, allow public list GETs when
     * [AppConfig.allowPublicLists] is true, and deny all mutating or authenticated
     * workspace/sync/API access not already permitted.
     */
    fun configureReadOnlyEndpoints(
        http: HttpSecurity,
        appConfig: AppConfig,
    ): HttpSecurity {
        configurePublicEndpoints(http)
        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILL_AUDIT_LOG))
                .denyAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTION_AUDIT_LOG))
                .denyAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_SKILLS))
                .denyAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.TASK_DETAIL_BATCH))
                .denyAll()
        }
        http.authorizeHttpRequests { auth ->
            if (appConfig.allowPublicLists) {
                auth
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILLS_LIST))
                    .permitAll()
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTIONS_LIST))
                    .permitAll()
            } else {
                auth
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.SKILLS_LIST))
                    .denyAll()
                    .requestMatchers(GET, *buildAllVersions(RoutePaths.COLLECTIONS_LIST))
                    .denyAll()
            }
        }
        return http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_UPDATE))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILLS_CREATE))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.SKILL_PUBLISH))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_CREATE))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_PUBLISH))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_UPDATE))
                .denyAll()
                .requestMatchers(POST, *buildAllVersions(RoutePaths.COLLECTION_SKILLS_UPDATE))
                .denyAll()
                .requestMatchers(DELETE, *buildAllVersions(RoutePaths.COLLECTION_REMOVE))
                .denyAll()
                .requestMatchers(GET, *buildAllVersions(RoutePaths.WORKSPACE_PATH))
                .denyAll()
                .requestMatchers(POST, "/api/sync/skill/**", "/api/sync/collection/**")
                .denyAll()
                .requestMatchers("/api/sync/**")
                .denyAll()
                .requestMatchers("/api/**")
                .denyAll()
                .requestMatchers("/**")
                .permitAll()
        }
    }
}
