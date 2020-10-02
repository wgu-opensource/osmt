package edu.wgu.osmt.keyword

import edu.wgu.osmt.BaseDockerizedTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class KeywordRepositoryTest: BaseDockerizedTest() {

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
        val byNameDao = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, value=keywordName)
        assertThat(byNameDao).isNotNull

        val byName = keywordRepository.findById(byNameDao!!.id.value)?.toModel()
        assertThat(byName).isNotNull
        assertThat(byName?.value).isEqualTo(keywordName)
        assertThat(byName?.uri).isNull()

        val relookupByName = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, value=keywordName)
        assertThat(relookupByName).isNotNull
        assertThat(relookupByName!!.id).isEqualTo(byNameDao.id)

        val byUriDao = keywordRepository.findOrCreate(KeywordTypeEnum.Standard, uri=keywordUri)
        assertThat(byUriDao).isNotNull
        val byUri = keywordRepository.findById(byUriDao!!.id.value)?.toModel()
        assertThat(byUri).isNotNull
        assertThat(byUri?.value).isNull()
        assertThat(byUri?.uri).isEqualTo(keywordUri)
    }
}

