package edu.wgu.osmt.api.model

import edu.wgu.osmt.jobcode.JobCodeTest
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApiSkillTest {

    private lateinit var mockData: MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun testApiSkillsCore() {
        // Arrange
        val rsd = mockData.getRichSkillDescriptors()[0]
        val cols = mockData.getCollections().toSet()

        // Act
        val actual = ApiSkill(rsd, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(mockData.appConfig.defaultCreatorUri).isEqualTo(actual.creator)
        Assertions.assertThat(rsd.author?.value).isEqualTo(actual.author)
        Assertions.assertThat(rsd.publishStatus()).isEqualTo(actual.status)
        Assertions.assertThat(rsd.creationDate).isEqualTo(actual.creationDate.toLocalDateTime())
        Assertions.assertThat(rsd.updateDate).isEqualTo(actual.updateDate.toLocalDateTime())
        Assertions.assertThat(rsd.publishDate).isEqualTo(actual.publishDate?.toLocalDateTime())
        Assertions.assertThat(rsd.archiveDate).isEqualTo(actual.archiveDate?.toLocalDateTime())
        Assertions.assertThat(rsd.name).isEqualTo(actual.skillName)
        Assertions.assertThat(rsd.statement).isEqualTo(actual.skillStatement)
        Assertions.assertThat(rsd.category?.value).isEqualTo(actual.category)
        Assertions.assertThat(mockData.appConfig.baseUrl + "/api/skills/" + rsd.uuid).isEqualTo(actual.id)
        Assertions.assertThat(rsd.uuid).isEqualTo(actual.uuid)

        Assertions.assertThat("https://rsd.openskillsnetwork.org/context-v1.json").isEqualTo(actual.context)
        Assertions.assertThat("RichSkillDescriptor").isEqualTo(actual.type)
    }

    @Test
    fun testApiSkillsCollections() {
        // Arrange
        val rsd = mockData.getRichSkillDescriptors()[0]
        val cols = mockData.getCollections().toSet()

        // Act
        val actual = ApiSkill(rsd, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.collections.size).isEqualTo(cols.size)
        for ((i, c) in cols.withIndex()) {
            Assertions.assertThat(c.uuid).isEqualTo(actual.collections[i].uuid)
            Assertions.assertThat(c.name).isEqualTo(actual.collections[i].name)
        }
    }

    @Test
    fun testApiSkillsKeywords() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        var rsd: RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.keywords.isNotEmpty()) {
                rsd = r
                break
            }
        }

        if (rsd == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val actual = ApiSkill(rsd!!, cols, mockData.appConfig)

        // Assert
        assertTrue(rsd.searchingKeywords.map { it.value }.containsAll(actual.keywords))
        assertTrue(actual.keywords.containsAll(rsd.searchingKeywords.map { it.value }))
    }

    @Test
    fun testApiSkillsJobCodes() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        var rsd: RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.jobCodes.isNotEmpty()) {
                rsd = r
                break
            }
        }

        if (rsd == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val actual = ApiSkill(rsd!!, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.occupations.size).isEqualTo(rsd.jobCodes.size)
        for (i in rsd.jobCodes.indices) {
            JobCodeTest.assertEquals(rsd.jobCodes[i], actual.occupations[i])
        }
    }

    @Test
    fun testApiSkillsCertifications() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        var rsd: RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.certifications.isNotEmpty()) {
                rsd = r
                break
            }
        }

        if (rsd == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val actual = ApiSkill(rsd!!, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.certifications.size).isEqualTo(rsd.certifications.size)
        assertTrue(rsd.certifications.map { it.value }.containsAll(actual.certifications.map { it.name }))
        assertTrue(actual.certifications.map { it.name }.containsAll(rsd.certifications.map { it.value }))
    }

    @Test
    fun testApiSkillsStandards() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        var rsd: RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.standards.isNotEmpty()) {
                rsd = r
                break
            }
        }

        if (rsd == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val actual = ApiSkill(rsd!!, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.standards.size).isEqualTo(rsd.standards.size)
        assertTrue(rsd.standards.map { it.value }.containsAll(actual.standards.map { it.skillName }))
        assertTrue(actual.standards.map { it.skillName }.containsAll(rsd.standards.map { it.value }))
    }

    @Test
    fun testApiSkillsAlignments() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        var rsd: RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.alignments.isNotEmpty()) {
                rsd = r
                break
            }
        }

        if (rsd == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val actual = ApiSkill(rsd!!, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.alignments.size).isEqualTo(rsd.alignments.size)
        assertTrue(rsd.alignments.map { it.value }.containsAll(actual.alignments.map { it.skillName }))
        assertTrue(actual.alignments.map { it.skillName }.containsAll(rsd.alignments.map { it.value }))
    }


    @Test
    fun testApiSkillsEmployers() {
        // Arrange
        val cols = mockData.getCollections().toSet()

        val rsd: RichSkillDescriptor = mockData.getRichSkillDescriptors()[0]

        // Act
        val actual = ApiSkill(rsd, cols, mockData.appConfig)

        // Assert
        Assertions.assertThat(actual.employers.size).isEqualTo(rsd.employers.size)
        assertTrue(rsd.employers.map { it.value }.containsAll(actual.employers.map { it.name }))
        assertTrue(actual.employers.map { it.name }.containsAll(rsd.employers.map { it.value }))
    }
}