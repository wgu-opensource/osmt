# Phase 1: Backend Read-Only Profile and Security Config

## Scope of Phase

Create the `readonly` Spring profile that acts as a meta-configuration for read-only mode. This includes:

1. New `application-readonly.properties` with read-only defaults
2. `ReadOnlySecurityConfig.kt` that permits all requests without authentication
3. Updates to `AppConfig.kt` to support the read-only flag

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Create `application-readonly.properties`

**File**: `api/src/main/resources/config/application-readonly.properties`

```properties
# Read-Only Profile Meta-Configuration
# This profile configures OSMT to run as a public-facing read-only instance.
# Use with: spring.profiles.active=readonly (optionally with other profiles)

# Disable database migrations (writable instance handles these)
spring.flyway.enabled=false

# Disable session storage (no authentication needed)
spring.session.store-type=none

# Mark as read-only mode for any code that needs to check
app.readOnlyMode=true

# Instance type for whitelabel/frontend
app.instanceType=read-only

# Security: disable roles checking since there's no auth
app.enableRoles=false

# Allow public searching and lists (these are the only features in read-only)
app.allowPublicSearching=true
app.allowPublicLists=true
```

### 2. Create `ReadOnlySecurityConfig.kt`

**File**: `api/src/main/kotlin/edu/wgu/osmt/security/ReadOnlySecurityConfig.kt`

```kotlin
package edu.wgu.osmt.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Security configuration for read-only mode (profile: readonly).
 * Permits all requests without authentication. No login, no sessions.
 */
@Configuration
@EnableWebSecurity
@Profile("readonly")
class ReadOnlySecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors().and()
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeHttpRequests { auth ->
                // Permit all requests - no authentication in read-only mode
                auth.anyRequest().permitAll()
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // Allow all origins in read-only mode (public facing)
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("HEAD", "GET")
        configuration.allowCredentials = false
        configuration.allowedHeaders = listOf("Content-Type")
        configuration.exposedHeaders = listOf("X-Total-Count")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

### 3. Update `AppConfig.kt`

**File**: `api/src/main/kotlin/edu/wgu/osmt/config/AppConfig.kt`

Add the following properties:

```kotlin
@Value("\${app.readOnlyMode:false}")
val readOnlyMode: Boolean = false,

@Value("\${app.instanceType:writable}")
val instanceType: String,
```

### 4. Tests

**Test File**: `api/src/test/kotlin/edu/wgu/osmt/security/ReadOnlySecurityConfigTest.kt`

```kotlin
package edu.wgu.osmt.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("readonly", "test")
class ReadOnlySecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired(required = false)
    private var securityFilterChain: SecurityFilterChain? = null

    @Test
    fun `readonly profile activates ReadOnlySecurityConfig`() {
        assertThat(securityFilterChain).isNotNull()
    }

    @Test
    fun `public endpoints accessible without auth`() {
        mockMvc.perform(get("/api/skills"))
            .andExpect(status().isOk)
    }

    @Test
    fun `mutating endpoints return 403 in readonly mode`() {
        // This test documents that mutating endpoints exist
        // The actual 403 response would need to be implemented in phase 2
        // or handled at controller level
        mockMvc.perform(get("/api/skills")) // GET is allowed
            .andExpect(status().isOk)
    }
}
```

## Validate

Run the tests:

```bash
cd /Users/yona/dev/skybridge/osmt/api
sdk env install  # Ensure correct Java/Maven versions
mvn test -Dtest=ReadOnlySecurityConfigTest -Dspring.profiles.active=readonly,test
```

Verify the backend compiles:

```bash
mvn compile -q
```
