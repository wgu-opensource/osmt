package edu.wgu.osmt.richskill

import edu.wgu.osmt.csv.BatchImportRichSkill
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.keywordGenerator
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.collection.CollectionSearchService
import edu.wgu.osmt.collection.EsCollectionRepository
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.jobcode.EsJobCodeRepository
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.EsKeywordRepository
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class RichSkillSearchServiceTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val esCollectionRepository: EsCollectionRepository,
    override val esKeywordRepository: EsKeywordRepository,
    override val esJobCodeRepository: EsJobCodeRepository
) : SpringTest(), HasDatabaseReset, HasElasticsearchReset {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Autowired
    lateinit var collectionEsRepo: EsCollectionRepository

    val authorString = "unit-test-author"

    @Test
    fun `Should insert a rich skill into elastic search`() {
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
            ), BatchImportRichSkill.user
        )?.toModel()!!

        val elasticModel = richSkillEsRepo.findByUuid(rsd.uuid, Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
    }

    @Test
    fun `Should update a rich skill in elastic search`() {
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
            ), BatchImportRichSkill.user
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
            richSkillEsRepo.findByUuid(updateResult?.uuid.toString(), Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
        assertThat(esRetrieved.searchingKeywords).contains(keywordToAdd.value)
        assertThat(esRetrieved.searchingKeywords).doesNotContain(keywordToRemove.toModel().value)
        assertThat(esRetrieved.name).isEqualTo(newName)
        assertThat(esRetrieved.statement).isEqualTo(newStatement)
    }


    fun queryCollectionHits(query: String): List<CollectionDoc> {
        return collectionEsRepo.byApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    fun queryRichSkillHits(query: String): List<RichSkillDoc> {
        return richSkillEsRepo.byApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    @Test
    fun `Should be able to query collections across multiple fields`() {
        // generate random documents to insert
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                richSkillEsRepo.save(it)
                esCollectionRepository.saveAll(it.collections)
            }
        }

        val collectionDoc = TestObjectHelpers.collectionDoc(name = "Name with Orange in it", author = "Ronald Purple")
        val elasticRichSkillDoc = TestObjectHelpers.randomRichSkillDoc().let {
            val cs = it.collections
            it.copy(collections = cs + collectionDoc)
        }.also {
            esCollectionRepository.saveAll(it.collections)
            richSkillEsRepo.save(it)
        }

        fun assertAgainstCollectionDoc(cs: List<CollectionDoc>) {
            cs.map {
                assertThat(it.uuid).isIn(elasticRichSkillDoc.collections.map { it.uuid })
            }
            //assertThat(elasticRichSkillDoc.collections.map { it.uuid }).contains(*cs.map { it.uuid }.toTypedArray())
        }

        val searchByCollectionName =
            collectionEsRepo.byApiSearch(ApiSearch(query = "orange")).searchHits.map { it.content }

        val searchByCollectionAuthor =
            collectionEsRepo.byApiSearch(ApiSearch(query = "purple")).searchHits.map { it.content }

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
                richSkillEsRepo.save(it)
                esCollectionRepository.saveAll(it.collections)
            }
        }

        val richSkillDoc = TestObjectHelpers.randomRichSkillDoc()

        fun assertAgainstRichSkillDoc(rsds: List<RichSkillDoc>) {
            assertThat(rsds.size).isEqualTo(1)
            assertThat(rsds[0].uuid).isEqualTo(richSkillDoc.uuid)
        }

        esCollectionRepository.saveAll(richSkillDoc.collections)
        richSkillEsRepo.save(richSkillDoc)

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

        val searchByCollectionName = richSkillEsRepo.byApiSearch(
            ApiSearch(query = richSkillDoc.collections[0].name)
        ).searchHits.map { it.content }

        assertThat(searchByCollectionName[0].uuid).isEqualTo(richSkillDoc.uuid)
    }

    @Test
    fun `Should limit rich skills search to a related collection if collection id present`() {
        val associatedCollection = TestObjectHelpers.collectionDoc(name = "Test Collection")


        val skillsWCollectionIds = (1..100).map {
            val doc = TestObjectHelpers.randomRichSkillDoc()
            doc.copy(collections = listOf(associatedCollection))
        }

        val nonCollectionSkills = (1..100).map {
            TestObjectHelpers.randomRichSkillDoc()
        }

        esCollectionRepository.save(associatedCollection)
        richSkillEsRepo.saveAll(skillsWCollectionIds + nonCollectionSkills)

        val collectionRelatedResults =
            richSkillEsRepo.byApiSearch(apiSearch = ApiSearch(), collectionId = associatedCollection.uuid)

        val allResults = richSkillEsRepo.byApiSearch(apiSearch = ApiSearch())

        val collectionRelatedSkillIds = collectionRelatedResults.searchHits.map { it.content.uuid }.toSet()
        val allResultsCollectionIds = allResults.searchHits.map { it.content.uuid }.toSet()

        assertThat(collectionRelatedSkillIds).isEqualTo(skillsWCollectionIds.map { it.uuid }.toSet())

        assertThat(allResultsCollectionIds).isEqualTo((skillsWCollectionIds + nonCollectionSkills).map { it.uuid }
            .toSet())
    }

    @Test
    fun `Should match on partial word queries`() {
        var collection = TestObjectHelpers.randomCollectionDoc()
            .copy(name = "A collection that questions the ability to perform partial query matches")
        val skill = TestObjectHelpers.randomRichSkillDoc().copy(
            name = "A skill that questions the ability to perform partial query matches",
            collections = listOf(collection)
        )
        collection = collection.copy(skillIds = listOf(skill.uuid))

        var otherCollections = (1..20).map { TestObjectHelpers.randomCollectionDoc() }
        val otherSkills = (1..100).map { TestObjectHelpers.randomRichSkillDoc().copy(collections = otherCollections) }
        otherCollections = otherCollections.map { it.copy(skillIds = otherSkills.map { it.uuid }) }

        richSkillEsRepo.saveAll(listOf(skill) + otherSkills)
        esCollectionRepository.saveAll(listOf(collection) + otherCollections)


        val skillSearchResult = richSkillEsRepo.byApiSearch(ApiSearch(query = "quest"))
        val collectionSearchResult = collectionEsRepo.byApiSearch(ApiSearch(query = "quest"))


        assertThat(skillSearchResult.searchHits.first().content.uuid).isEqualTo(skill.uuid)
        assertThat(skillSearchResult.totalHits).isEqualTo(1L)

        assertThat(collectionSearchResult.searchHits.first().content.uuid).isEqualTo(collection.uuid)
        assertThat(collectionSearchResult.totalHits).isEqualTo(1L)
    }

    @Test
    fun `Should allow prefix searches on advanced search job codes`() {
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                richSkillEsRepo.save(it)
                esCollectionRepository.saveAll(it.collections)
            }
        }

        val jobCodes = listOf(
            JobCode.create("10-9999.88").copy(id = TestObjectHelpers.elasticIdCounter)
        )

        val skillWithJobCodes = TestObjectHelpers.randomRichSkillDoc().copy(jobCodes = jobCodes).also {
            richSkillEsRepo.save(it)
        }

        val searchResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        ApiNamedReference(name = "10-99")
                    )
                )
            )
        )

        assertThat(searchResult.searchHits.first().content.uuid).isEqualTo(skillWithJobCodes.uuid)
    }

    @Test
    fun `Should allow prefix searches on keywords`() {
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                richSkillEsRepo.save(it)
                esCollectionRepository.saveAll(it.collections)
            }
        }

        val searchingKeywords = listOf("SEL: Interpersonal Communication", "Doing", "Verbal Communication Skills")

        val skillWithKeywords =
            TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = searchingKeywords).also {
                richSkillEsRepo.save(it)
            }

        val searchResult =
            richSkillEsRepo.byApiSearch(ApiSearch(advanced = ApiAdvancedSearch(keywords = listOf("doing"))))
        val searchResult2 =
            richSkillEsRepo.byApiSearch(ApiSearch(advanced = ApiAdvancedSearch(keywords = listOf("sel"))))
        val searchResult3 =
            richSkillEsRepo.byApiSearch(ApiSearch(advanced = ApiAdvancedSearch(keywords = listOf("nada"))))

        assertThat(searchResult.searchHits.first().content.uuid).isEqualTo(skillWithKeywords.uuid)
        assertThat(searchResult2.searchHits.first().content.uuid).isEqualTo(skillWithKeywords.uuid)
        assertThat(searchResult3.searchHits).isEmpty()
    }

    @Test
    fun `Should allow advanced searches with lists`() {
        val keywords = listOf("red", "orange", "yellow", "green", "blue", "indigo", "violet")

        val richSkill1 = TestObjectHelpers.randomRichSkillDoc().copy(standards = keywords)
        val richSkill2 = TestObjectHelpers.randomRichSkillDoc().copy(certifications = keywords)
        val richSkill3 = TestObjectHelpers.randomRichSkillDoc().copy(employers = keywords)
        val richSkill4 = TestObjectHelpers.randomRichSkillDoc().copy(alignments = keywords)

        richSkillEsRepo.saveAll(listOf(richSkill1, richSkill2, richSkill3, richSkill4))

        val result1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    standards = listOf(
                        ApiNamedReference(name = "red"),
                        ApiNamedReference(name = "orange")
                    )
                )
            )
        )
        val result2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    certifications = listOf(
                        ApiNamedReference(name = "yellow"),
                        ApiNamedReference(name = "green")
                    )
                )
            )
        )
        val result3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    employers = listOf(
                        ApiNamedReference(name = "blue"),
                        ApiNamedReference(name = "indigo")
                    )
                )
            )
        )
        val result4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    alignments = listOf(ApiNamedReference(name = "violet"))
                )
            )
        )

        assertThat(result1.searchHits.first().content.uuid).isEqualTo(richSkill1.uuid)
        assertThat(result2.searchHits.first().content.uuid).isEqualTo(richSkill2.uuid)
        assertThat(result3.searchHits.first().content.uuid).isEqualTo(richSkill3.uuid)
        assertThat(result4.searchHits.first().content.uuid).isEqualTo(richSkill4.uuid)
    }
}
