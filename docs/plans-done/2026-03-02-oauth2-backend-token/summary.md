# OAuth2 Backend-Issued Token – Plan Summary

## Goal

Replace OAuth2 pass-through of IdP token with backend-issued signed JWT. Fixes logout bug when viewing public page in new tab; keeps role/scope logic on backend; consistent token format for frontend.

## Completed Work

- **AppConfig**: Added sessionTokenSecret, sessionTokenExpirySeconds, sessionTokenIssuer
- **SessionTokenJwtConfig**: JwtEncoder + JwtDecoder with HS256, issuer validation
- **SessionTokenService**: Creates signed JWT from OidcUser (roles, sub, email, name)
- **RedirectToFrontend**: Uses SessionTokenService.createToken() instead of idToken
- **SecurityConfig**: Injects our JwtDecoder, applies jwtAuthenticationConverter for roles
- **SingleAuthAwareJwtDecoderConfig**: Delegates non-admin tokens to sessionTokenJwtDecoder
- **Frontend storeToken**: Simplified to roles claim only (with array support)
- **auth.md**: Documented APP_SESSION_TOKEN_* env vars
- **MockData.kt**: Updated createAppConfig() with new parameters
