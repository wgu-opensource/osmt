package edu.wgu.osmt.api.model

import edu.wgu.osmt.jobcode.JobCodeTest
import edu.wgu.osmt.mockdata.MockData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApiSkillSummaryTest {

    private lateinit var mockData : MockData

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun verifyFromDoc() {
        // Arrange
        var expected: edu.wgu.osmt.richskill.RichSkillDoc? = null
        for (r in mockData.getRichSkillDocs()) {
            if (r.jobCodes.isNotEmpty()) {
                expected = r
                break
            }
        }

        if (expected == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        // Act
        val api = ApiSkillSummary.fromDoc(expected!!)

        // Assert
        Assertions.assertThat(api.uuid).isEqualTo(expected.uuid)
        Assertions.assertThat(api.status.toString()).isEqualTo(expected.publishStatus.toString())
        Assertions.assertThat(api.publishDate).isEqualTo(expected.publishDate)
        Assertions.assertThat(api.archiveDate).isEqualTo(expected.archiveDate)
        Assertions.assertThat(api.skillName).isEqualTo(expected.name)
        Assertions.assertThat(api.skillStatement).isEqualTo(expected.statement)
        Assertions.assertThat(api.category).isEqualTo(expected.category)

        // Search Keywords
        assertTrue(expected.searchingKeywords.containsAll(api.keywords))
        assertTrue(api.keywords.containsAll(expected.searchingKeywords))

        // jobCodes
        Assertions.assertThat(api.occupations.size).isEqualTo(expected.jobCodes.size)
        for (i in expected.jobCodes.indices) {
            JobCodeTest.assertEquals(expected.jobCodes[i], api.occupations[i])
        }
    }

    @Test
    fun testFromSkill() {
        // Arrange
        var expected: edu.wgu.osmt.richskill.RichSkillDescriptor? = null
        for (r in mockData.getRichSkillDescriptors()) {
            if (r.jobCodes.isNotEmpty()) {
                expected = r
                break
            }
        }

        if (expected == null) {
            Assertions.fail<Any>("Unable to find suitable RichSkillDoc")
        }

        val id = mockData.appConfig.baseUrl + "/api/skills/" + expected!!.uuid

        // Act
        val skill = ApiSkillSummary.fromSkill(expected, mockData.appConfig)

        // Assert
        Assertions.assertThat(skill.id).isEqualTo(id)
        Assertions.assertThat(skill.uuid).isEqualTo(expected.uuid)
        Assertions.assertThat(skill.status).isEqualTo(expected.publishStatus())
        Assertions.assertThat(skill.publishDate).isEqualTo(expected.publishDate)
        Assertions.assertThat(skill.archiveDate).isEqualTo(expected.archiveDate)
        Assertions.assertThat(skill.skillName).isEqualTo(expected.name)
        Assertions.assertThat(skill.skillStatement).isEqualTo(expected.statement)
        Assertions.assertThat(skill.category).isEqualTo(expected.category?.value)

        // Search Keywords
        assertTrue(expected.keywords.map{it.value}.containsAll(skill.keywords))
        assertTrue(skill.keywords.containsAll(expected.searchingKeywords.map{it.value}))

        // jobCodes
        Assertions.assertThat(skill.occupations.size).isEqualTo(expected.jobCodes.size)
        for (i in expected.jobCodes.indices) {
            JobCodeTest.assertEquals(expected.jobCodes[i], skill.occupations[i])
        }
    }

}