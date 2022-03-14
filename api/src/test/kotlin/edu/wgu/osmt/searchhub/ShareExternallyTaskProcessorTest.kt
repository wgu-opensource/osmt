package edu.wgu.osmt.searchhub

import com.ninjasquad.springmockk.MockkBean
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.searchhub.client.models.Result
import edu.wgu.osmt.task.ShareExternallyTask
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@TestConfiguration
class ShareExternallyTaskProcessorTestConfiguration {
    @Bean
    fun shareExternallyTaskProcessor(): ShareExternallyTaskProcessor {
        return ShareExternallyTaskProcessor()
    }
}

@Import(ShareExternallyTaskProcessorTestConfiguration::class)
@ExtendWith(SpringExtension::class)
@TestPropertySource( properties = [
    "app.searchhub.enabled=true",
    "app.searchhub.baseUrl=http://searchhub.test",
    "app.searchhub.accessToken=token1"
])
internal class ShareExternallyTaskProcessorTest @Autowired constructor(): SpringTest(), BaseDockerizedTest {

    @MockkBean
    private lateinit var searchHubController: SearchHubController

    @Autowired
    private lateinit var taskProcessor: ShareExternallyTaskProcessor

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `test process share skill`(){
        // Arrange
        val skill = mockData.getRichSkillDescriptors()[0]
        val task = ShareExternallyTask(
            canonicalUrl = skill.canonicalUrl("http://test.test"),
            shared = true
        )

        val searchHubResult = Result(
            uuid = UUID.randomUUID(),
            status = Result.Status.processing,
            contentType = "<contentType>"
        )

        every { searchHubController.submitSkills(any()) } returns searchHubResult

        // Act
        taskProcessor.processShareSkill(task)
    }

    @Test
    fun `test process unshare skill`(){
        // Arrange
        val skill = mockData.getRichSkillDescriptors()[0]
        val task = ShareExternallyTask(
            canonicalUrl = skill.canonicalUrl("http://test.test"),
            shared = false
        )

        val searchHubResult = Result(
            uuid = UUID.randomUUID(),
            status = Result.Status.processing,
            contentType = "<contentType>"
        )

        every { searchHubController.removeSkills(any()) } returns searchHubResult

        // Act
        taskProcessor.processShareSkill(task)
    }

    @Test
    fun `test process share collection`(){
        // Arrange
        val collection = mockData.getCollections()[0]
        val task = ShareExternallyTask(
            canonicalUrl = collection.canonicalUrl("http://test.test"),
            shared = true
        )

        val searchHubResult = Result(
            uuid = UUID.randomUUID(),
            status = Result.Status.processing,
            contentType = "<contentType>"
        )

        every { searchHubController.submitCollections(any()) } returns searchHubResult

        // Act
        taskProcessor.processShareCollection(task)
    }

    @Test
    fun `test process unshare collection`(){
        // Arrange
        val collection = mockData.getCollections()[0]
        val task = ShareExternallyTask(
            canonicalUrl = collection.canonicalUrl("http://test.test"),
            shared = false
        )

        val searchHubResult = Result(
            uuid = UUID.randomUUID(),
            status = Result.Status.processing,
            contentType = "<contentType>"
        )

        every { searchHubController.removeCollections(any()) } returns searchHubResult

        // Act
        taskProcessor.processShareCollection(task)
    }
}
