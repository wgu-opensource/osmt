package edu.wgu.osmt.keyword

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiAlignment
import edu.wgu.osmt.api.model.ApiAlignmentListUpdate
import edu.wgu.osmt.api.model.ApiNamedReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Transactional
class KeywordRepositoryTest: SpringTest(), HasDatabaseReset {

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Test
    fun `should not create keyword with blank value and uri`() {
        var keyword = keywordRepository.findOrCreate(KeywordTypeEnum.Keyword, value=null, uri=null)
        assertThat(keyword).isNull()

        keyword = keywordRepository.create(KeywordTypeEnum.Category, value=null, uri=null)
        assertThat(keyword).isNull()
    }

    @Test
    fun `should findOrCreate keywords by uri or value`() {
        val keywordName = UUID.randomUUID().toString()
        val keywordUri = UUID.randomUUID().toString()

        val byNameAndUri = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, value=keywordName, uri=keywordUri)?.toModel()
        assertThat(byNameAndUri).isNotNull

        val byNameDao = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, value=keywordName)
        assertThat(byNameDao).isNotNull

        val byName = keywordRepository.findById(byNameDao!!.id.value)?.toModel()
        assertThat(byName).isNotNull
        assertThat(byName?.id).isNotEqualTo(byNameAndUri?.id)
        assertThat(byName?.value).isEqualTo(keywordName)
        assertThat(byName?.uri).isNull()

        val relookupByName = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, value=keywordName)
        assertThat(relookupByName).isNotNull
        assertThat(relookupByName!!.id).isEqualTo(byNameDao.id)

        val byUriDao = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, uri=keywordUri)
        assertThat(byUriDao).isNotNull
        val byUri = keywordRepository.findById(byUriDao!!.id.value)?.toModel()
        assertThat(byUri).isNotNull
        assertThat(byUri?.id).isNotEqualTo(byNameAndUri?.id)
        assertThat(byUri?.value).isNull()
        assertThat(byUri?.uri).isEqualTo(keywordUri)

    }

    @Test
    fun `should (de)serialize all ApiAlignment fields correctly`() {
        val mapper = jacksonObjectMapper()
        val frameworkName = "frameworkName"
        val frameworkRef = ApiNamedReference(name=frameworkName)
        val alignment = ApiAlignment(id="http://skill.test/test-skill", skillName="Test Skill", isPartOf=frameworkRef)
        val jsonstr = mapper.writeValueAsString(alignment)

        val expectedJson = "{\"id\":\"${alignment.id}\",\"skillName\":\"${alignment.skillName}\",\"isPartOf\":{\"name\":\"${frameworkName}\"}}"
        assertThat(jsonstr).isEqualTo(expectedJson)

        val readAlignment: ApiAlignment = mapper.readValue(expectedJson, ApiAlignment::class.java)
        assertThat(readAlignment).isEqualTo(alignment)

        val alu = ApiAlignmentListUpdate(add=listOf(alignment))
        val aluStr = mapper.writeValueAsString(alu)
        val expectedAlu = "{\"add\":[${expectedJson}],\"remove\":null}"
        assertThat(aluStr).isEqualTo(expectedAlu)

        val readAlu: ApiAlignmentListUpdate = mapper.readValue(expectedAlu, ApiAlignmentListUpdate::class.java)
        assertThat(readAlu).isEqualTo(alu)

    }

}

