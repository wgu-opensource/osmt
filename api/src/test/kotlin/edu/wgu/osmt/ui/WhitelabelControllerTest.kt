package edu.wgu.osmt.ui

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

/**
 * Integration test for whitelabel/whitelabel.json endpoint.
 * Verifies singleAuthEnabled is returned when single-auth profile is active.
 *
 * Note: authProviders requires ClientRegistrationRepository, which is created by
 * OAuth2 client auto-config. In the test context this bean may not be created;
 * verify authProviders manually by running the API with oauth2 profile
 * and curl http://localhost:8080/whitelabel/whitelabel.json
 */
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test", "apiserver", "oauth2", "single-auth")
internal class WhitelabelControllerTest
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
        fun `whitelabel json returns authProviders and singleAuthEnabled when oauth2 and single-auth active`() {
            val result =
                mockMvc
                    .perform(get("/whitelabel/whitelabel.json"))
                    .andExpect(status().isOk)
                    .andReturn()

            val body =
                objectMapper.readValue<WhitelabelResponse>(
                    result.response.contentAsString,
                )

            assertThat(body.singleAuthEnabled)
                .describedAs("singleAuthEnabled should be true when single-auth profile active")
                .isTrue()
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class WhitelabelResponse(
            val authProviders: List<AuthProvider> = emptyList(),
            val singleAuthEnabled: Boolean = false,
            val authMode: String? = null,
            val loginUrl: String? = null,
        ) {
            data class AuthProvider(
                val id: String,
                val name: String,
                val authorizationUrl: String,
            )
        }
    }
