# Phase 6: Update SingleAuthAwareJwtDecoderConfig for Staging

## Scope of phase

When both oauth2 and single-auth are enabled (staging), the JwtDecoder must accept (a) admin-jwt-* tokens from single-auth login, and (b) our session tokens from OAuth2 login. Update SingleAuthAwareJwtDecoderConfig to handle both.

## Code Organization Reminders

- Keep delegation logic clear.
- Place conditional checks first.

## Implementation Details

### 1. Current behavior

SingleAuthAwareJwtDecoderConfig activates for `@Profile("oauth2 & single-auth")`. It provides a JwtDecoder that:
- If token starts with `admin-jwt-`: build Jwt in-memory with admin claims
- Else: delegate to `oauth2Decoder` (IdP issuer-based)

### 2. New behavior

- If token starts with `admin-jwt-`: build Jwt in-memory (unchanged)
- Else: delegate to our session token decoder (validate our JWT)

We no longer delegate to the IdP decoder. All non-admin tokens are our session tokens.

### 3. Implementation

Inject the session token JwtDecoder (from SessionTokenJwtConfig). SessionTokenJwtConfig is `@Profile("oauth2")` and SingleAuthAwareJwtDecoderConfig is `@Profile("oauth2 & single-auth")`, so when the latter is active, the former is also active. Inject `sessionTokenJwtDecoder`:

```kotlin
@Configuration
@Profile("oauth2 & single-auth")
class SingleAuthAwareJwtDecoderConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private val issuerUri: String,
    private val sessionTokenJwtDecoder: JwtDecoder,  // from SessionTokenJwtConfig
) {
    @Bean
    fun jwtDecoder(): JwtDecoder =
        JwtDecoder { token ->
            if (token.startsWith(ADMIN_JWT_PREFIX)) {
                createAdminJwt(token)
            } else {
                sessionTokenJwtDecoder.decode(token)
            }
        }
    // ... createAdminJwt unchanged, remove oauth2Decoder
}
```

**Conflict**: SessionTokenJwtConfig provides `jwtDecoder()` (or `sessionTokenJwtDecoder()`). SingleAuthAwareJwtDecoderConfig also provides `jwtDecoder()`. When both profiles are active, we have two configs. SessionTokenJwtConfig is `oauth2`; SingleAuthAwareJwtDecoderConfig is `oauth2 & single-auth`. So when staging (oauth2 + single-auth), BOTH are active. We'll get two JwtDecoder beans. SingleAuthAwareJwtDecoderConfig's jwtDecoder should be the one used (it handles both cases). SessionTokenJwtConfig should NOT provide a bean named `jwtDecoder` when single-auth is enabled – only the `sessionTokenJwtDecoder` bean for injection.

**Resolution**: In SessionTokenJwtConfig, provide only `sessionTokenJwtDecoder`. For oauth2-only, we need a `jwtDecoder` bean. So:
- SessionTokenJwtConfig: provides `sessionTokenJwtDecoder` and `jwtDecoder` (which returns sessionTokenJwtDecoder) when oauth2. Use `@Profile("oauth2 & !single-auth")` for the jwtDecoder bean? No – that gets complex.
- Simpler: SessionTokenJwtConfig always provides `sessionTokenJwtDecoder`. SessionTokenJwtConfig provides `jwtDecoder` only when `oauth2 & !single-auth` (oauth2 only). SingleAuthAwareJwtDecoderConfig provides `jwtDecoder` when `oauth2 & single-auth`, and it uses sessionTokenJwtDecoder for non-admin tokens.

So we need conditional bean:
- `oauth2` profile: need jwtDecoder. SessionTokenJwtConfig provides it (returns sessionTokenJwtDecoder).
- `oauth2 & single-auth`: SingleAuthAwareJwtDecoderConfig provides jwtDecoder (delegates to admin or sessionTokenJwtDecoder). SessionTokenJwtConfig should NOT provide jwtDecoder to avoid conflict.

Option: SessionTokenJwtConfig provides `sessionTokenJwtDecoder` only. Add a separate small config that provides `jwtDecoder` for `oauth2 & !single-auth`. Or: SingleAuthAwareJwtDecoderConfig has `@ConditionalOnProperty` or `@Profile` so it only provides its bean when single-auth is enabled. When single-auth is disabled, no SingleAuthAwareJwtDecoderConfig – then we need another config to provide jwtDecoder for oauth2-only. That could be SessionTokenJwtConfig: provide both sessionTokenJwtDecoder and jwtDecoder, but make jwtDecoder conditional on `!singleAuthEnabled`? That's a runtime condition, not profile.

Cleaner: Create `OAuth2JwtDecoderConfig` with `@Profile("oauth2")` that provides `jwtDecoder`. It delegates to sessionTokenJwtDecoder. When `oauth2 & single-auth`, SingleAuthAwareJwtDecoderConfig ALSO provides jwtDecoder – bean conflict. Use `@Primary` on SingleAuthAwareJwtDecoderConfig's decoder when it's present. So:
- SessionTokenJwtConfig (oauth2): sessionTokenJwtEncoder, sessionTokenJwtDecoder
- OAuth2JwtDecoderConfig (oauth2): jwtDecoder = sessionTokenJwtDecoder. This is the default for oauth2.
- SingleAuthAwareJwtDecoderConfig (oauth2 & single-auth): jwtDecoder = composite (admin or session). This OVERRIDES the default when both profiles active.

We need OAuth2JwtDecoderConfig to provide jwtDecoder for oauth2-only. And SingleAuthAwareJwtDecoderConfig overrides for staging. The way to override is to have SingleAuthAwareJwtDecoderConfig's jwtDecoder bean take precedence. In Spring, when two @Configuration classes both define the same bean, the one with more specific @Profile wins. `oauth2 & single-auth` is more specific than `oauth2`. So when both active, SingleAuthAwareJwtDecoderConfig's jwtDecoder wins. When only oauth2, only SessionTokenJwtConfig and OAuth2JwtDecoderConfig (or we combine) are active.

Let me simplify: Have SessionTokenJwtConfig provide `jwtDecoder` that returns our decoder. Have SingleAuthAwareJwtDecoderConfig also provide `jwtDecoder` but with `@Profile("oauth2 & single-auth")` and order it to load after. The more specific profile wins. So when oauth2 & single-auth, SingleAuthAwareJwtDecoderConfig's jwtDecoder is used. When oauth2 only, SessionTokenJwtConfig's jwtDecoder is used. But SessionTokenJwtConfig is @Profile("oauth2") – it will also load for oauth2 & single-auth. So we'd have two jwtDecoder beans. We need @Primary or @Order. Use @Primary on SingleAuthAwareJwtDecoderConfig's decoder so when both exist, staging wins.

Actually the simpler approach: SingleAuthAwareJwtDecoderConfig only activates for oauth2 & single-auth. When it activates, it provides jwtDecoder. SessionTokenJwtConfig activates for oauth2. When oauth2 & single-auth, BOTH activate. SessionTokenJwtConfig should NOT provide a bean named jwtDecoder – only sessionTokenJwtDecoder. Then we need another config for oauth2-only that provides jwtDecoder. Create OAuth2JwtDecoderConfig @Profile("oauth2") that provides jwtDecoder = sessionTokenJwtDecoder. But when single-auth is also enabled, we'd have OAuth2JwtDecoderConfig (provides jwtDecoder) AND SingleAuthAwareJwtDecoderConfig (provides jwtDecoder). Two beans. Use @ConditionalOnMissingBean or @Profile on the oauth2-only jwtDecoder: OAuth2JwtDecoderConfig provides jwtDecoder with @Profile("oauth2") and @ConditionalOnMissingBean(JwtDecoder::class)? No, both would try to provide it when staging.

Simplest: SingleAuthAwareJwtDecoderConfig provides jwtDecoder. It's @Profile("oauth2 & single-auth"). For oauth2-only, we need something to provide jwtDecoder. SessionTokenJwtConfig could have a method `jwtDecoder()` that returns sessionTokenJwtDecoder, but only when SingleAuthAwareJwtDecoderConfig is NOT active. Use @Profile on the jwtDecoder bean in SessionTokenJwtConfig: @Profile("oauth2") – that would still load for staging. Use @Profile("oauth2") on the class and @Bean @Profile("!single-auth") on jwtDecoder? Profile "!single-auth" – but single-auth is a profile. When we have oauth2 and single-auth, single-auth is in the active profiles, so "!single-auth" would be false. So that bean wouldn't load. Good. So SessionTokenJwtConfig has:
```kotlin
@Bean
@Profile("oauth2")
@ConditionalOnNotProfile("single-auth")  // doesn't exist
```
Use `@Profile("oauth2 & !single-auth")` for the oauth2-only jwtDecoder bean in SessionTokenJwtConfig. Use `@Profile("oauth2 & single-auth")` for SingleAuthAwareJwtDecoderConfig. No overlap.

## Validate

```bash
cd api && mvn test -Dtest=*SingleAuth* -q
```
