package edu.wgu.osmt.searchhub

import com.ninjasquad.springmockk.MockkBean
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.ImportExternalService
import edu.wgu.osmt.richskill.ImportExternalTaskProcessor
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.ShareExternallyTask
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.select
import org.springframework.transaction.annotation.Transactional

@TestConfiguration
class ImportExternalTaskProcessorTestConfiguration {
    @Bean
    fun importExternalTaskProcessor(): ImportExternalTaskProcessor {
        return ImportExternalTaskProcessor()
    }
}


@Import(ImportExternalTaskProcessorTestConfiguration::class)
@ExtendWith(SpringExtension::class)
@Transactional
internal class ImportExternalTaskProcessorTest @Autowired constructor(): SpringTest(), BaseDockerizedTest, HasDatabaseReset {

    @MockkBean
    private lateinit var importExternalService: ImportExternalService

    @Autowired
    private lateinit var taskProcessor: ImportExternalTaskProcessor

    @Autowired
    private lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    private lateinit var mockData : MockData

    val userString = "unittestuser"

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `should import external skill`() {
        val externalLibraryName = "External Library"
        val externalSkill = mockData.getRichSkillDescriptors().first()
        val externalCanonicalUrl = externalSkill.canonicalUrl(mockData.appConfig.baseUrl)

        val task = ShareExternallyTask(
            canonicalUrl = externalCanonicalUrl,
            libraryName = externalLibraryName,
            userString = userString
        )

        val apiSkill = ApiSkill.fromModel(externalSkill, setOf(), mockData.appConfig)

        every {
            importExternalService.fetchSkillFromUrl(externalCanonicalUrl)
        } returns apiSkill

        taskProcessor.processImportSkill(task)

        val localSkillDao = richSkillRepository.findByOriginalUrl(externalCanonicalUrl)

        assertThat(localSkillDao).isNotNull
        assertThat(localSkillDao?.libraryName).isEqualTo(externalLibraryName)
    }

    @Test
    fun `should import an external collection and all its skills`() {
        val externalLibraryName = "External Library"
        val externalBaseUrl = mockData.appConfig.baseUrl

        val collection: Collection = mockData.getCollections().first()
        val skills = mockData.getRichSkillDescriptors()

        val apiCollection = ApiCollection.fromModel(collection, skills, mockData.appConfig)
        every {
            importExternalService.fetchCollectionFromUrl(collection.canonicalUrl(externalBaseUrl))
        } returns apiCollection

        skills.forEach { skill ->
            val apiSkill = ApiSkill.fromModel(skill, setOf(), mockData.appConfig)
            every {
                importExternalService.fetchSkillFromUrl(skill.canonicalUrl(externalBaseUrl))
            } returns apiSkill
        }

        val task = ShareExternallyTask(
                canonicalUrl = collection.canonicalUrl(externalBaseUrl),
                libraryName = externalLibraryName,
                userString = userString
        )
        taskProcessor.processImportCollection(task)

        val localCollectionDao = collectionRepository.findByOriginalUrl(collection.canonicalUrl(externalBaseUrl))
        assertThat(localCollectionDao).isNotNull
        assertThat(localCollectionDao?.libraryName).isEqualTo(externalLibraryName)

        skills.forEach { skill ->
            val skillDao = richSkillRepository.findByOriginalUrl(skill.canonicalUrl(externalBaseUrl))
            assertThat(skillDao).isNotNull
            assertThat(skillDao?.libraryName).isEqualTo(externalLibraryName)
        }

    }
}