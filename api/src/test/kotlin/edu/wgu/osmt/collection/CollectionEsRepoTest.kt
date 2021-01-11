package edu.wgu.osmt.collection

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.richskill.QuotedSearchHelpers
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class CollectionEsRepoTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo
): SpringTest(), HasDatabaseReset, HasElasticsearchReset, QuotedSearchHelpers {

    fun queryCollectionHits(query: String): List<CollectionDoc> {
        return collectionEsRepo.byApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    @Test
    fun `Should be able to query collections across multiple fields`() {
        // generate random documents to insert
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                richSkillEsRepo.save(it)
                collectionEsRepo.saveAll(it.collections)
            }
        }

        val collectionDoc = TestObjectHelpers.collectionDoc(name = "Name with Orange in it", author = "Ronald Purple")
        val elasticRichSkillDoc = TestObjectHelpers.randomRichSkillDoc().let {
            val cs = it.collections
            it.copy(collections = cs + collectionDoc)
        }.also {
            collectionEsRepo.saveAll(it.collections)
            richSkillEsRepo.save(it)
        }

        fun assertAgainstCollectionDoc(cs: List<CollectionDoc>) {
            cs.map {
                Assertions.assertThat(it.uuid).isIn(elasticRichSkillDoc.collections.map { it.uuid })
            }
            //assertThat(elasticRichSkillDoc.collections.map { it.uuid }).contains(*cs.map { it.uuid }.toTypedArray())
        }

        val searchByCollectionName =
            collectionEsRepo.byApiSearch(ApiSearch(query = "orange")).searchHits.map { it.content }

        val searchByCollectionAuthor =
            collectionEsRepo.byApiSearch(ApiSearch(query = "purple")).searchHits.map { it.content }

        Assertions.assertThat(searchByCollectionName[0].uuid).isEqualTo(collectionDoc.uuid)
        Assertions.assertThat(searchByCollectionAuthor[0].uuid).isEqualTo(collectionDoc.uuid)

        // assertions by rich skill properties
        val results1 = queryCollectionHits(elasticRichSkillDoc.name)
        assertAgainstCollectionDoc(results1)
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.author!!))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.statement))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.searchingKeywords[elasticRichSkillDoc.searchingKeywords.indices.random()]))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.category!!))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.jobCodes[elasticRichSkillDoc.jobCodes.indices.random()].code))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.standards[elasticRichSkillDoc.standards.indices.random()]))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.certifications[elasticRichSkillDoc.certifications.indices.random()]))
        assertAgainstCollectionDoc(queryCollectionHits(elasticRichSkillDoc.employers[elasticRichSkillDoc.employers.indices.random()]))
    }

    @Test
    fun `Should perform simple quoted searches`(){
        val searchSetupResults = quotedSearchSetup()

        val ambiguousUnQuotedSearch = collectionEsRepo.byApiSearch(ApiSearch(query= "Self-Management")).searchHits.map{ it.content }
        val quotedSearch = collectionEsRepo.byApiSearch(ApiSearch(query= "\"Self-Management\"")).searchHits.map{ it.content }

        assertThat(quotedSearch).contains(searchSetupResults.collections.first())
        assertThat(quotedSearch.size).isEqualTo(1)
    }

    @Test
    fun `Should perform advanced quoted searches`(){
        val searchSetupResults = quotedSearchSetup()

        val advancedQuotedSearch = collectionEsRepo.byApiSearch(ApiSearch(advanced = ApiAdvancedSearch(collectionName = "\"Self-Management\""))).searchHits.map{ it.content }

        assertThat(advancedQuotedSearch).contains(searchSetupResults.collections.first())
        assertThat(advancedQuotedSearch.size).isEqualTo(1)
    }
}
