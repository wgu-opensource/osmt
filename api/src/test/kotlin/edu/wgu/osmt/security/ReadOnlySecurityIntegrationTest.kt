package edu.wgu.osmt.security

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
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

/**
 * Read-only profile: public routes work; mutating API calls receive 403;
 * whitelabel exposes readOnlyMode.
 */
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test", "apiserver", "readonly")
@TestPropertySource(
    properties = [
        "spring.flyway.enabled=true",
        "spring.session.store-type=redis",
    ],
)
internal class ReadOnlySecurityIntegrationTest
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
        fun `whitelabel returns readOnlyMode true`() {
            val result =
                mockMvc
                    .perform(get("/whitelabel/whitelabel.json"))
                    .andExpect(status().isOk)
                    .andReturn()
            val body =
                objectMapper.readValue<WhitelabelSplitBody>(
                    result.response.contentAsString,
                )
            assertThat(body.readOnlyMode).isTrue()
        }

        @Test
        fun `post create skill returns forbidden on read-only instance`() {
            mockMvc
                .perform(
                    post("/api/v3/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"),
                ).andExpect(status().isForbidden)
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class WhitelabelSplitBody(
            val readOnlyMode: Boolean? = null,
        )
    }
