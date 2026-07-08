package edu.wgu.osmt.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

/**
 * Verifies single-auth login works when both oauth2 and single-auth profiles are active.
 *
 * Bug fix: When both profiles are enabled, the OAuth2 resource server's JwtDecoder
 * rejected single-auth tokens (admin-jwt-*), causing 401 on subsequent API requests.
 * SingleAuthAwareJwtDecoderConfig handles admin-jwt-* tokens alongside OAuth2 JWTs.
 */
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test", "apiserver", "oauth2", "single-auth")
internal class SingleAuthWithOAuth2IntegrationTest
    @Autowired
    constructor(
        val mockMvc: MockMvc,
        override val collectionEsRepo: edu.wgu.osmt.collection.CollectionEsRepo,
        override val keywordEsRepo: edu.wgu.osmt.keyword.KeywordEsRepo,
        override val jobCodeEsRepo: edu.wgu.osmt.jobcode.JobCodeEsRepo,
        override val richSkillEsRepo: edu.wgu.osmt.richskill.RichSkillEsRepo,
    ) : SpringTest(),
        BaseDockerizedTest,
        HasDatabaseReset,
        HasElasticsearchReset {
        private val objectMapper = jacksonObjectMapper()

        @Test
        fun `single-auth login token is accepted for authenticated API requests`() {
            val loginBody = """{"username":"admin","password":"admin"}"""
            val loginResult =
                mockMvc
                    .perform(
                        post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody),
                    ).andExpect(status().isOk)
                    .andReturn()

            val loginResponse =
                objectMapper.readValue<LoginResponse>(loginResult.response.contentAsString)
            assertThat(loginResponse.token).isNotBlank()
            assertThat(loginResponse.tokenType).isEqualTo("Bearer")

            // Workspace requires authentication (ADMIN or CURATOR)
            val workspacePath =
                "${RoutePaths.API}${RoutePaths.API_V3}${RoutePaths.WORKSPACE_PATH}"
            mockMvc
                .perform(
                    get(workspacePath)
                        .header("Authorization", "Bearer ${loginResponse.token}"),
                ).andExpect(status().isOk)
        }

        private data class LoginResponse(
            val token: String,
            val expiresIn: Long,
            val tokenType: String = "Bearer",
        )
    }
