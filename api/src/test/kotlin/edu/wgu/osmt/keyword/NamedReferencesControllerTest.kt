package edu.wgu.osmt.keyword

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

@Transactional
internal class NamedReferencesControllerTest @Autowired constructor(
    val keywordRepository: KeywordRepository,
    val keywordEsRepo: KeywordEsRepo
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset {

    @Autowired
    lateinit var namedReferencesController: NamedReferencesController

    val dao = KeywordDao.Companion

    @Test
    fun `Index should return an array with almost one named reference`() {
        dao.new {
            this.uri = "uri"
            this.value = "value"
            this.framework = "my framework"
            this.type = KeywordTypeEnum.Keyword
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
        }.also { keywordEsRepo.save(it.toModel()) }
        val result = namedReferencesController.allPaginated(KeywordTypeEnum.Keyword.toString(), 50, 0, null)
        Assertions.assertThat(result.body).hasSizeGreaterThan(0)
    }

    @Test
    fun `By id should find a named reference`() {
        dao.new {
            this.uri = "uri"
            this.value = "value"
            this.framework = "my framework"
            this.type = KeywordTypeEnum.Keyword
            this.creationDate = LocalDateTime.now(ZoneOffset.UTC)
            this.updateDate = LocalDateTime.now(ZoneOffset.UTC)
        }.also { keywordEsRepo.save(it.toModel()) }
        val result = namedReferencesController.byId(1)
        Assertions.assertThat(result.body).isNotNull
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Create should return created job codes`() {
        val result = namedReferencesController.createNamedReference(
            listOf(ApiKeywordUpdate("my keyword", "my value", KeywordTypeEnum.Keyword, "my framework"))
        )
        Assertions.assertThat(result.body).hasSizeGreaterThan(0)
    }

    @Test
    fun `Update should return job codes with updated properties`() {
        val result = namedReferencesController.updateNamedReference(
            1,
            ApiKeywordUpdate("my keyword", "my value", KeywordTypeEnum.Keyword, "my framework")
        )
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Delete should return status 200`() {
        val result = namedReferencesController.deleteNamedReference(
            1
        )
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat((result as ResponseEntity).statusCode).isEqualTo(HttpStatus.OK)
    }

}