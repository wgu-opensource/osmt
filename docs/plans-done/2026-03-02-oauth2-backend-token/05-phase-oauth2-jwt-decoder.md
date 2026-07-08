# Phase 5: Wire OAuth2 Resource Server to Use Our JwtDecoder

## Scope of phase

Configure the OAuth2 resource server to use our session token JwtDecoder instead of the default IdP issuer-based decoder. Our decoder validates JWTs signed with our secret and our issuer.

## Code Organization Reminders

- Keep security config clear; avoid duplicate decoder beans.

## Implementation Details

### 1. Provide JwtDecoder bean for oauth2 resource server

When `oauth2` profile is active, the resource server needs a JwtDecoder. Spring Boot auto-configures one from `spring.security.oauth2.resourceserver.jwt.issuer-uri`. We override by providing our own `JwtDecoder` bean in SessionTokenJwtConfig.

Name the bean or ensure it's the only JwtDecoder when oauth2 is active. The resource server picks up `JwtDecoder` bean by type.

**Issue**: SessionTokenJwtConfig provides `sessionTokenJwtDecoder()` – the bean name will be `sessionTokenJwtDecoder`, not `jwtDecoder`. The resource server typically looks for `JwtDecoder` or `jwtDecoder`. Check Spring Boot OAuth2 resource server auto-config – it may use `@Qualifier` or `@Primary`.

**Approach**: Provide a `@Bean fun jwtDecoder(): JwtDecoder` that returns our decoder. In SessionTokenJwtConfig, add:

```kotlin
@Bean
@Primary
fun jwtDecoder(): JwtDecoder = sessionTokenJwtDecoder()
```

Or simply name the existing method `jwtDecoder` and remove the `sessionToken` prefix for the bean that the resource server consumes.

### 2. Disable or override issuer-based auto-configuration

When we provide our JwtDecoder bean, Spring Boot's OAuth2 resource server should use it and not create its own. Verify that `spring.security.oauth2.resourceserver.jwt.issuer-uri` is not required when a custom decoder is present. We may need to exclude the auto-config or set a placeholder for issuer-uri if it's required.

### 3. Verify decoder validates issuer

NimbusJwtDecoder by default may not validate issuer. Use:

```kotlin
NimbusJwtDecoder.withSecretKey(key)
    .macAlgorithm(MacAlgorithm.HS256)
    .build()
```

To add issuer validation, use `JwtValidators.createDefault()` or set `setJwtValidator` with an issuer claim validator. Example:

```kotlin
val decoder = NimbusJwtDecoder.withSecretKey(key)
    .macAlgorithm(MacAlgorithm.HS256)
    .build()
decoder.setJwtValidator(
    JwtValidators.createDefaultWithIssuer(appConfig.sessionTokenIssuer)
)
return decoder
```

### 4. Update SecurityConfig oauth2ResourceServer

SecurityConfig currently has:
```kotlin
oauth2.jwt { jwt ->
    if (appConfig.singleAuthEnabled) {
        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
    }
}
```

With our JwtDecoder bean, no changes needed – the resource server uses our bean. The jwtAuthenticationConverter is for mapping claims to authorities; our JWT has `roles` claim, so the existing converter (which uses oauth2RolesClaim) should work. Ensure jwtAuthenticationConverter is applied when oauth2 is active (not just when singleAuthEnabled). Check current logic – it may only set the converter for staging. For oauth2-only, we need the converter to map `roles` to authorities. Set it unconditionally for oauth2.

## Validate

```bash
cd api && mvn test -Dtest=*OAuth* -q
```

Run OAuth-related tests. Manual test: complete OAuth2 login, verify redirect has token, call API with Bearer token.
