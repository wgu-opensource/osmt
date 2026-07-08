# Phase 4: Update RedirectToFrontend

## Scope of phase

Modify RedirectToFrontend to call SessionTokenService.createToken(oidcUser) and pass the result in the redirect URL instead of oidcUser.idToken.tokenValue.

## Code Organization Reminders

- Keep RedirectToFrontend focused on redirect logic.

## Implementation Details

### 1. Inject SessionTokenService

RedirectToFrontend is in SecurityConfig.kt (same file). Add SessionTokenService via constructor (or @Autowired lateinit like appConfig):

```kotlin
@Component
class RedirectToFrontend(
    @Autowired lateinit var appConfig: AppConfig,
    @Autowired lateinit var sessionTokenService: SessionTokenService,
) : AuthenticationSuccessHandler {
```

### 2. Use SessionTokenService in onAuthenticationSuccess

```kotlin
override fun onAuthenticationSuccess(
    request: HttpServletRequest?,
    response: HttpServletResponse?,
    authentication: Authentication?,
) {
    val redirectStrategy = DefaultRedirectStrategy()
    when (authentication?.principal) {
        is OidcUser -> {
            val oidcUser = authentication.principal as OidcUser
            val token = sessionTokenService.createToken(oidcUser)
            val url = "${appConfig.loginSuccessRedirectUrl}?token=$token"
            redirectStrategy.sendRedirect(request, response, url)
        }
    }
}
```

### 3. Profile

RedirectToFrontend is used by SecurityConfig which is `@Profile("oauth2")`. SessionTokenService is also `@Profile("oauth2")`, so they align.

## Validate

```bash
cd api && mvn compile -q
```
