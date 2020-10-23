package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.BatchImportConsoleApplication
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.keywordGenerator
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class SearchServiceTest : SpringTest(), HasDatabaseReset {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Autowired
    lateinit var searchService: SearchService

    val authorString = "unit-test-author"

    @Test
    fun `should insert a rich skill into elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordGenerator(10, KeywordTypeEnum.Employer)

        val allKeywords = keywords + employers

        val allKeywordDao = allKeywords.mapNotNull { keywordRepository.create(it.type, it.value) }

        val rsd = richSkillRepository.create(
            RsdUpdateObject(
                name = name,
                statement = statement,
                keywords = ListFieldUpdate(add = allKeywordDao)
            ), BatchImportConsoleApplication.user
        )?.toModel()!!

        val elasticModel = searchService.esRichSkillRepository.findByUuid(rsd.uuid, Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
    }

    @Test
    fun `should update a rich skill in elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordGenerator(10, KeywordTypeEnum.Employer)

        val allKeywords = keywords + employers

        val allKeywordDao = allKeywords.mapNotNull { keywordRepository.create(it.type, it.value) }

        val rsd = richSkillRepository.create(
            RsdUpdateObject(
                name = name,
                statement = statement,
                keywords = ListFieldUpdate(add = allKeywordDao)
            ), BatchImportConsoleApplication.user
        )?.toModel()!!

        val newName = UUID.randomUUID().toString()
        val newStatement = UUID.randomUUID().toString()

        val keywordToRemove = allKeywordDao.get(0)
        val keywordToAdd = keywordGenerator(1, KeywordTypeEnum.Keyword)[0]
        val keywordToAddDao = keywordRepository.create(keywordToAdd.type, keywordToAdd.value)

        val updateObject = RsdUpdateObject(
            id = rsd.id,
            name = newName,
            statement = newStatement,
            keywords = ListFieldUpdate(add = listOf(keywordToAddDao!!), remove = listOf(keywordToRemove))
        )
        val updateResult = richSkillRepository.update(updateObject, "test-user")

        val elasticModel =
            searchService.esRichSkillRepository.findByUuid(updateResult?.uuid.toString(), Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
        assertThat(esRetrieved.searchingKeywords).contains(keywordToAdd.value)
        assertThat(esRetrieved.searchingKeywords).doesNotContain(keywordToRemove.toModel().value)
        assertThat(esRetrieved.name).isEqualTo(newName)
        assertThat(esRetrieved.statement).isEqualTo(newStatement)
    }


    fun queryCollectionHits(query: String): List<CollectionDoc> {
        return searchService.searchCollectionsByApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    fun queryRichSkillHits(query: String): List<RichSkillDoc> {
        return searchService.searchRichSkillsByApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    @Test
    fun `Should be able to query collections across multiple fields`() {
        // generate random documents to insert
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                searchService.esRichSkillRepository.save(it)
                searchService.esCollectionRepository.saveAll(it.collections)
            }
        }

        val collectionDoc = TestObjectHelpers.collectionDoc(name = "Name with Orange in it", author = "Ronald Purple")
        val elasticRichSkillDoc = TestObjectHelpers.randomRichSkillDoc().let {
            val cs = it.collections
            it.copy(collections = cs + collectionDoc)
        }.also {
            searchService.esCollectionRepository.saveAll(it.collections)
            searchService.esRichSkillRepository.save(it)
        }

        fun assertAgainstCollectionDoc(cs: List<CollectionDoc>) {
            cs.map {
                assertThat(it.uuid).isIn(elasticRichSkillDoc.collections.map { it.uuid })
            }
            //assertThat(elasticRichSkillDoc.collections.map { it.uuid }).contains(*cs.map { it.uuid }.toTypedArray())
        }

        val searchByCollectionName =
            searchService.searchCollectionsByApiSearch(ApiSearch(query = "orange")).searchHits.map { it.content }

        val searchByCollectionAuthor =
            searchService.searchCollectionsByApiSearch(ApiSearch(query = "purple")).searchHits.map { it.content }

        assertThat(searchByCollectionName[0].uuid).isEqualTo(collectionDoc.uuid)
        assertThat(searchByCollectionAuthor[0].uuid).isEqualTo(collectionDoc.uuid)

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
    fun `Should be able to query rich skills across multiple fields`() {
        // generate random documents to insert
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                searchService.esRichSkillRepository.save(it)
                searchService.esCollectionRepository.saveAll(it.collections)
            }
        }

        val richSkillDoc = TestObjectHelpers.randomRichSkillDoc()

        fun assertAgainstRichSkillDoc(rsds: List<RichSkillDoc>) {
            assertThat(rsds.size).isEqualTo(1)
            assertThat(rsds[0].uuid).isEqualTo(richSkillDoc.uuid)
        }

        searchService.esCollectionRepository.saveAll(richSkillDoc.collections)
        searchService.esRichSkillRepository.save(richSkillDoc)

        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.name))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.author!!))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.statement))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.searchingKeywords[richSkillDoc.searchingKeywords.indices.random()]))

        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.category!!))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.jobCodes[richSkillDoc.jobCodes.indices.random()].code))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.standards[richSkillDoc.standards.indices.random()]))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.certifications[richSkillDoc.certifications.indices.random()]))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.certifications[richSkillDoc.certifications.indices.random()]))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.employers[richSkillDoc.employers.indices.random()]))

        val searchByCollectionName = searchService.searchRichSkillsByApiSearch(
            ApiSearch(query = richSkillDoc.collections[0].name)
        ).searchHits.map { it.content }

        assertThat(searchByCollectionName[0].uuid).isEqualTo(richSkillDoc.uuid)
    }
}
