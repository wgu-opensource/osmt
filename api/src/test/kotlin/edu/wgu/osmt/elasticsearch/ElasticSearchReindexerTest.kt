package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.QuotedSearchHelpers
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class ElasticSearchReindexerTest @Autowired constructor(
    val richSkillRepository: RichSkillRepository
) : SpringTest(), HasDatabaseReset, HasElasticsearchReset, QuotedSearchHelpers {

    @Autowired
    lateinit var elasticSearchReindexer: ElasticSearchReindexer

    @MockBean
    override lateinit var richSkillEsRepo: RichSkillEsRepo

    @MockBean
    override lateinit var collectionEsRepo: CollectionEsRepo

    @MockBean
    override lateinit var keywordEsRepo: KeywordEsRepo

    @MockBean
    override lateinit var jobCodeEsRepo: JobCodeEsRepo

    @Test
    fun testReindexAll() {
        // Arrange
        // This creates 1 skill, 3 collections, 17 keywords and 3 jobCodes
        richSkillRepository.createFromApi(
            (1..1).toList().map { TestObjectHelpers.apiSkillUpdateGenerator() }, "testReindexAll", "testReindexAll"
        )

        // Act
        // Assert
        Assertions.assertDoesNotThrow { elasticSearchReindexer.reindexAll() }

        // Each is called twice, one from the createFromApi, another one from the reindexAll.
        Mockito.verify(this.richSkillEsRepo, Mockito.times(2)).save(any())
        Mockito.verify(this.collectionEsRepo, Mockito.times(6)).save(any())
        Mockito.verify(this.keywordEsRepo, Mockito.times(42)).save(any())
        Mockito.verify(this.jobCodeEsRepo, Mockito.times(6)).save(any())

    }
}