package edu.wgu.osmt.api.model

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired

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

        // Act
        val api: ApiKeyword = ApiKeyword(kw, 7, mockData.appConfig)

        // Assert
        Assertions.assertThat(api.id).isEqualTo(kw.id)
        Assertions.assertThat(api.type).isEqualTo(kw.type)
        Assertions.assertThat(api.name).isEqualTo(kw.value)
    }
}
