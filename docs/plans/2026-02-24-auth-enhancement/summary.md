# Auth Enhancement Plan – Summary

## Completed Work

### Phase 1: Migrate to Standard Spring OAuth2
- Removed Okta Spring Boot starter; added spring-boot-starter-oauth2-resource-server
- Migrated application-oauth2-okta.properties to standard Spring format (spring.security.oauth2.client.registration.*)
- Created application-oauth2-google.properties for Google provider
- Added app.oauth2.rolesClaim to AppConfig; configurable via application.properties
- Added GrantedAuthoritiesMapper bean for roles-claim mapping from OIDC tokens
- Updated docker_entrypoint.sh and bin/lib/common.sh for oauth2-okta, oauth2-google, and single-auth profiles

### Phase 2: Unified Security and Whitelabel API
- Created AuthConfigProvider to expose OAuth providers to whitelabel
- Extended whitelabel API with authProviders and singleAuthEnabled
- Unified SecurityConfig to optionally add AdminUserAuthenticationFilter when singleAuthEnabled
- SingleAuthSecurityConfig now excludes when oauth2-okta or oauth2-google active (avoids duplicate filter chains)
- Added ENABLE_SINGLE_AUTH for staging (Google + single-auth)

### Phase 3: Frontend App Config
- Added AuthProvider interface and authProviders, singleAuthEnabled to IAppConfig
- Updated AppConfig.load() to merge authProviders and singleAuthEnabled from whitelabel

### Phase 4: Login Page Redesign
- Redesigned login with skill-form layout (l-stickySidebar, m-skillBackground, m-iconTitle)
- OAuth provider picker when multiple providers or single-auth enabled
- Single-auth form when singleAuthEnabled
- Auto-redirect when exactly one OAuth provider and no single-auth

### Phase 5: Documentation
- Added OAuth2 with Google section to README
- Added Staging: Google + Single-Auth section
- Created api/osmt-staging.env.example
- Updated api/osmt-dev-stack.env.example with Google and ENABLE_SINGLE_AUTH

### Phase 6: Cleanup
- API compiles; MockData updated for new AppConfig params
- UI builds and tests pass

## Migration Notes

**Existing Okta deployments:** No env var changes. OAUTH_CLIENTID, OAUTH_CLIENTSECRET, OAUTH_ISSUER, OAUTH_AUDIENCE still work. Profile remains oauth2-okta.

**New Google deployments:** Set OAUTH_GOOGLE_CLIENT_ID and OAUTH_GOOGLE_CLIENT_SECRET. Redirect URI: `{baseUrl}/login/oauth2/code/google`.

**Staging (Google + single-auth):** Set Google vars, ENABLE_SINGLE_AUTH=true, and admin credentials.
