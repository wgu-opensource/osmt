package edu.wgu.osmt.api.model

import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApiCollectionTest {

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun testApiCollection() {
        // Arrange
        val col = mockData.getCollection(1)

        // Act
        val api = col?.let { ApiCollection(it, ArrayList(), mockData.appConfig) }

        // Assert
        Assertions.assertThat(col?.creationDate).isEqualTo(api?.creationDate?.toLocalDateTime())
        Assertions.assertThat(col?.updateDate).isEqualTo(api?.updateDate?.toLocalDateTime())
        Assertions.assertThat(col?.uuid).isEqualTo(api?.uuid)
        Assertions.assertThat(col?.name).isEqualTo(api?.name)
        Assertions.assertThat(col?.description).isEqualTo(api?.description)
        Assertions.assertThat(col?.author?.value).isEqualTo(api?.author)
        Assertions.assertThat(col?.archiveDate).isEqualTo(api?.archiveDate?.toLocalDateTime())
        Assertions.assertThat(col?.publishDate).isEqualTo(api?.publishDate?.toLocalDateTime())
    }
}