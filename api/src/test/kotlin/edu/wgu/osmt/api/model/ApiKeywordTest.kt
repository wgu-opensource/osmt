package edu.wgu.osmt.api.model

import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApiKeywordTest {

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun testApiKeyword() {
        // Arrange
        val kw: Keyword = mockData.getKeywords().first()
        val skillsCount: Long = 7

        // Act
        val api: ApiKeyword = ApiKeyword(kw, skillsCount)

        // Assert
        Assertions.assertThat(api.id).isEqualTo(kw.id)
        Assertions.assertThat(api.type).isEqualTo(kw.type)
        Assertions.assertThat(api.skillCount).isEqualTo(skillsCount)
        Assertions.assertThat(api.value).isEqualTo(kw.value)
    }
}