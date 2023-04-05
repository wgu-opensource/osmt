package edu.wgu.osmt.richskill

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.keywordsGenerator
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiFilteredSearch
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSimilaritySearch
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.io.csv.BatchImportRichSkill
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface QuotedSearchHelpers {
    val richSkillEsRepo: RichSkillEsRepo
    val collectionEsRepo: CollectionEsRepo

    data class SearchSetupResults(val collections: List<CollectionDoc>, val skills: List<RichSkillDoc>)

    fun quotedSearchSetup(): SearchSetupResults {
        var collection1 = TestObjectHelpers.collectionDoc(name = "Self-Management Collection", description = "Collection of Self-Management Skills")
        val collection2 = TestObjectHelpers.collectionDoc(name = "Best Self Management Collection", description = "Collection of the best Self Management Skills")
        val randomCollections = (1..10).map{ TestObjectHelpers.randomCollectionDoc() }

        val skill1 = TestObjectHelpers.richSkillDoc(name = "Self-Management", statement = "A statement for a skill").copy(collections = listOf(collection1))
        val skill2 = TestObjectHelpers.richSkillDoc(name = "Self Mis-Management", statement = "A statement for a skill")
        val skill3 = TestObjectHelpers.richSkillDoc(name = "Best Self Management", statement = "A statement for a skill").copy(collections = listOf(collection2))
        val skill4 = TestObjectHelpers.richSkillDoc(name = "Management of Selfies", statement = "A statement for a skill")

        collection1 = collection1.copy(skillIds = listOf(skill1.uuid, skill2.uuid))

        val randomSkills = (1..10).map{TestObjectHelpers.randomRichSkillDoc()}
        richSkillEsRepo.saveAll(randomSkills + listOf(skill1, skill2, skill3, skill4))
        collectionEsRepo.saveAll(listOf(collection1,collection2) + randomCollections)
        return SearchSetupResults(listOf(collection1, collection2), listOf(skill1, skill2, skill3, skill4))
    }
}

@Transactional
class RichSkillEsRepoTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo,
    val richSkillRepository: RichSkillRepository,
    val keywordRepository: KeywordRepository
) : SpringTest(), HasDatabaseReset, HasElasticsearchReset, QuotedSearchHelpers {

    val authorString = "unit-test-author"

    @Test
    fun `Should insert a rich skill into elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordsGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordsGenerator(10, KeywordTypeEnum.Employer)

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

        val keywords = keywordsGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordsGenerator(10, KeywordTypeEnum.Employer)

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
        val keywordToAdd = keywordsGenerator(1, KeywordTypeEnum.Keyword)[0]
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

    fun queryRichSkillHits(query: String): List<RichSkillDoc> {
        return richSkillEsRepo.byApiSearch(ApiSearch(query)).searchHits.map { it.content }
    }

    @Test
    fun `Should be able to query rich skills across multiple fields`() {
        // generate random documents to insert
        (1..100).map {
            TestObjectHelpers.randomRichSkillDoc().also {
                richSkillEsRepo.save(it)
                collectionEsRepo.saveAll(it.collections)
            }
        }

        val richSkillDoc = TestObjectHelpers.randomRichSkillDoc()

        fun assertAgainstRichSkillDoc(rsds: List<RichSkillDoc>) {
            assertThat(rsds.size).isEqualTo(1)
            assertThat(rsds[0].uuid).isEqualTo(richSkillDoc.uuid)
        }

        collectionEsRepo.saveAll(richSkillDoc.collections)
        richSkillEsRepo.save(richSkillDoc)

        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.name))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.authors[richSkillDoc.authors.indices.random()]))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.statement))
        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.searchingKeywords[richSkillDoc.searchingKeywords.indices.random()]))

        assertAgainstRichSkillDoc(queryRichSkillHits(richSkillDoc.categories[richSkillDoc.categories.indices.random()]))
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
        val associatedCollection = TestObjectHelpers.collectionDoc(name = "Test Collection", description = "Collection of Test Skills")


        val skillsWCollectionIds = (1..100).map {
            val doc = TestObjectHelpers.randomRichSkillDoc()
            doc.copy(collections = listOf(associatedCollection))
        }

        val nonCollectionSkills = (1..100).map {
            TestObjectHelpers.randomRichSkillDoc()
        }

        collectionEsRepo.save(associatedCollection)
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
        collectionEsRepo.saveAll(listOf(collection) + otherCollections)


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
                collectionEsRepo.saveAll(it.collections)
            }
        }

        val jobCodes = listOf(
            TestObjectHelpers.randomJobCode().copy(code = "10-9999.88", id = TestObjectHelpers.elasticIdCounter)
        )

        val skillWithJobCodes = TestObjectHelpers.randomRichSkillDoc().copy(jobCodes = jobCodes).also {
            richSkillEsRepo.save(it)
        }

        val searchResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        "10-99"
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
                collectionEsRepo.saveAll(it.collections)
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


    @Test
    fun `Should perform simple quoted searches`(){
        val searchSetupResults = quotedSearchSetup()

        val ambiguousNonQuotedSearch = richSkillEsRepo.byApiSearch(ApiSearch(query = "self management")).searchHits.map{it.content}
        val quotedSearch = richSkillEsRepo.byApiSearch(ApiSearch(query= "\"Self-Management\"")).searchHits.map{it.content}

        assertThat(quotedSearch).contains(searchSetupResults.skills.first())
        assertThat(quotedSearch.size).isEqualTo(1)
    }

    @Test
    fun `Should perform advanced quoted searches`(){
        val searchSetupResults = quotedSearchSetup()
        val advancedQuotedSearch = richSkillEsRepo.byApiSearch(ApiSearch(advanced = ApiAdvancedSearch(skillName = "\"Self-Management\""))).searchHits.map{ it.content }

        assertThat(advancedQuotedSearch).contains(searchSetupResults.skills.first())
        assertThat(advancedQuotedSearch.size).isEqualTo(1)
    }


    @Test
    fun testByApiSearchWithAdvance() {
        // Arrange
        val categoriesList = listOf("Category1", "Category2", "Category3", "Category4","Category5", "Category6")
        val authorsList = listOf("Author1", "Author2", "Author3", "Author4", "Author5", "Author6")
        val keywordslist = listOf("One", "Two", "Three", "Four", "Five", "Six")

        val skillByName = TestObjectHelpers.randomRichSkillDoc().copy(name = "skillName")
        val skillByCategory = TestObjectHelpers.randomRichSkillDoc().copy(categories = categoriesList)
        val skillByAuthor = TestObjectHelpers.randomRichSkillDoc().copy(authors = authorsList)
        val skillByStatement = TestObjectHelpers.randomRichSkillDoc().copy(statement = "skillStatement")
        val skillByKeywords = TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = keywordslist)
        val skillByStandards = TestObjectHelpers.randomRichSkillDoc().copy(standards = keywordslist)
        val skillByCertifications = TestObjectHelpers.randomRichSkillDoc().copy(certifications = keywordslist)
        val skillByEmployers = TestObjectHelpers.randomRichSkillDoc().copy(employers = keywordslist)
        val skillByAlignments = TestObjectHelpers.randomRichSkillDoc().copy(alignments = keywordslist)

        richSkillEsRepo.saveAll(listOf(skillByName, skillByCategory, skillByAuthor, skillByStatement,skillByKeywords,
                skillByStandards,skillByCertifications,skillByEmployers,skillByAlignments))

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                skillName="skillName"
                        )
                )
        )

        val categoryResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                category = "category2"
                        )
                )
        )

        val authorResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                author = "author"
                        )
                )
        )

        val skillStatementResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                skillStatement = "skillStatement"
                        )
                )
        )

        val keywordsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                keywords = keywordslist

                        )
                )
        )

        val standardsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                standards = listOf(
                                        ApiNamedReference(name = "One"),
                                        ApiNamedReference(name = "Two")
                                )
                        )
                )
        )

        val certificationsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                certifications = listOf(
                                        ApiNamedReference(name = "Three"),
                                        ApiNamedReference(name = "Four")
                                )
                        )
                )
        )

        val employersResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                employers = listOf(
                                        ApiNamedReference(name = "Five"),
                                        ApiNamedReference(name = "Six")
                                )
                        )
                )
        )

        val alignmentsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                alignments = listOf(
                                        ApiNamedReference(name = "Six"),
                                        ApiNamedReference(name = "One")
                                )
                        )
                )
        )

        // Assert
        assertThat(skillResult.searchHits.first().content.uuid).isEqualTo(skillByName.uuid)
        assertThat(categoryResult.searchHits.first().content.uuid).isEqualTo(skillByCategory.uuid)
        assertThat(authorResult.searchHits.first().content.uuid).isEqualTo(skillByAuthor.uuid)
        assertThat(skillStatementResult.searchHits.first().content.uuid).isEqualTo(skillByStatement.uuid)
        assertThat(keywordsResult.searchHits.first().content.uuid).isEqualTo(skillByKeywords.uuid)
        assertThat(standardsResult.searchHits.first().content.uuid).isEqualTo(skillByStandards.uuid)
        assertThat(certificationsResult.searchHits.first().content.uuid).isEqualTo(skillByCertifications.uuid)
        assertThat(employersResult.searchHits.first().content.uuid).isEqualTo(skillByEmployers.uuid)
        assertThat(alignmentsResult.searchHits.first().content.uuid).isEqualTo(skillByAlignments.uuid)

    }

    @Test
    fun testByApiSearchWithSimpleQuoted() {
        // Arrange
        val categoriesList = listOf("Category1", "Category2", "Category3", "Category4","Category5", "Category6")
        val authorsList = listOf("Author1", "Author2", "Author3", "Author4", "Author5", "Author6")
        val keywordslist = listOf("One", "Two", "Three", "Four", "Five", "Six")

        val skillByName = TestObjectHelpers.randomRichSkillDoc().copy(name = "skillName")
        val skillByCategory = TestObjectHelpers.randomRichSkillDoc().copy(categories = categoriesList)
        val skillByAuthor = TestObjectHelpers.randomRichSkillDoc().copy(authors = authorsList)
        val skillByStatement = TestObjectHelpers.randomRichSkillDoc().copy(statement = "skillStatement")
        val skillByKeywords = TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = keywordslist)
        val skillByStandards = TestObjectHelpers.randomRichSkillDoc().copy(standards = keywordslist)
        val skillByCertifications = TestObjectHelpers.randomRichSkillDoc().copy(certifications = keywordslist)
        val skillByEmployers = TestObjectHelpers.randomRichSkillDoc().copy(employers = keywordslist)
        val skillByAlignments = TestObjectHelpers.randomRichSkillDoc().copy(alignments = keywordslist)

        richSkillEsRepo.saveAll(listOf(skillByName, skillByCategory, skillByAuthor, skillByStatement,skillByKeywords,skillByStandards,skillByCertifications,skillByEmployers,skillByAlignments))

        // Act
        val skillResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                skillName="\"skillName\""
                        )
                )
        )

        val categoryResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                category = "\"category4\""
                        )
                )
        )

        val authorResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                author = "\"author3\""
                        )
                )
        )

        val skillStatementResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                skillStatement = "\"skillStatement\""
                        )
                )
        )

        val keywordsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                keywords = listOf("\"One\"", "\"Two\"", "\"Three\"", "\"Four\"", "\"Five\"", "\"Six\"")

                        )
                )
        )

        val standardsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                standards = listOf(
                                        ApiNamedReference(name = "\"One\""),
                                        ApiNamedReference(name = "\"Two\"")
                                )
                        )
                )
        )

        val certificationsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                certifications = listOf(
                                        ApiNamedReference(name = "\"Three\""),
                                        ApiNamedReference(name = "\"Four\"")
                                )
                        )
                )
        )

        val employersResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                employers = listOf(
                                        ApiNamedReference(name = "\"Five\""),
                                        ApiNamedReference(name = "\"Six\"")
                                )
                        )
                )
        )

        val alignmentsResult = richSkillEsRepo.byApiSearch(
                ApiSearch(
                        advanced = ApiAdvancedSearch(
                                alignments = listOf(
                                        ApiNamedReference(name = "\"Six\""),
                                        ApiNamedReference(name = "\"One\"")
                                )
                        )
                )
        )

        // Assert
        assertThat(skillResult.searchHits.first().content.uuid).isEqualTo(skillByName.uuid)
        assertThat(categoryResult.searchHits.first().content.uuid).isEqualTo(skillByCategory.uuid)
        assertThat(authorResult.searchHits.first().content.uuid).isEqualTo(skillByAuthor.uuid)
        assertThat(skillStatementResult.searchHits.first().content.uuid).isEqualTo(skillByStatement.uuid)
        assertThat(keywordsResult.searchHits.first().content.uuid).isEqualTo(skillByKeywords.uuid)
        assertThat(standardsResult.searchHits.first().content.uuid).isEqualTo(skillByStandards.uuid)
        assertThat(certificationsResult.searchHits.first().content.uuid).isEqualTo(skillByCertifications.uuid)
        assertThat(employersResult.searchHits.first().content.uuid).isEqualTo(skillByEmployers.uuid)
        assertThat(alignmentsResult.searchHits.first().content.uuid).isEqualTo(skillByAlignments.uuid)

    }
    @Test
    fun `search with categories filter should apply to filtered search`() {
        // Arrange
        val skillWithCategory1 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category1"))
        val skillWithCategory2 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category2"))
        val skillWithCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category3"))

        richSkillEsRepo.saveAll(listOf(skillWithCategory1,skillWithCategory2,skillWithCategory3))

        // Act
        val filteredSearchResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult.searchHits.first().content.uuid).isEqualTo(skillWithCategory1.uuid)
        assertThat(filteredSearchResult.searchHits[1].content.uuid).isEqualTo(skillWithCategory3.uuid)
    }
    @Test
    fun `search with keywords filter should apply to filtered search with AND operator between keywords`() {
        // Arrange
        val skillWithKeywords1and2 = TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = listOf("keyword1", "keyword2"))
        val skillWithKeywords2and3 = TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = listOf("keyword2", "keyword3"))
        val skillWithKeywords3and4 = TestObjectHelpers.randomRichSkillDoc().copy(searchingKeywords = listOf("keyword3", "keyword4"))

        richSkillEsRepo.saveAll(listOf(skillWithKeywords1and2,skillWithKeywords2and3,skillWithKeywords3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    keywords = listOf("keyword1", "keyword2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    keywords = listOf("keyword2", "keyword3")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    keywords = listOf("keyword3", "keyword4")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    keywords = listOf("keyword1", "keyword3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithKeywords1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithKeywords2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithKeywords3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()
    }
    @Test
    fun `search with standards filter should apply to filtered search with AND operator between standards`() {
        // Arrange
        val skillWithStandards1and2 = TestObjectHelpers.randomRichSkillDoc().copy(standards = listOf("standard1", "standard2"))
        val skillWithStandards2and3 = TestObjectHelpers.randomRichSkillDoc().copy(standards = listOf("standard2", "standard3"))
        val skillWithStandards3and4 = TestObjectHelpers.randomRichSkillDoc().copy(standards = listOf("standard3", "standard4"))

        richSkillEsRepo.saveAll(listOf(skillWithStandards1and2,skillWithStandards2and3,skillWithStandards3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    standards = listOf("standard1", "standard2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    standards = listOf("standard2", "standard3")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    standards = listOf("standard3", "standard4")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    standards = listOf("standard1", "standard3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithStandards1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithStandards2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithStandards3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()
    }
    @Test
    fun `Search with certifications filter should apply to filtered search with AND operator between certifications`() {
        // Arrange
        val skillWithCertifications1and2 = TestObjectHelpers.randomRichSkillDoc().copy(certifications = listOf("certification1", "certification2"))
        val skillWithCertifications2and3 = TestObjectHelpers.randomRichSkillDoc().copy(certifications = listOf("certification2", "certification3"))
        val skillWithCertifications3and4 = TestObjectHelpers.randomRichSkillDoc().copy(certifications = listOf("certification3", "certification4"))

        richSkillEsRepo.saveAll(listOf(skillWithCertifications1and2,skillWithCertifications2and3,skillWithCertifications3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    certifications = listOf("certification1", "certification2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    certifications = listOf("certification2", "certification3")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    certifications = listOf("certification3", "certification4")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    certifications = listOf("certification1", "certification3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithCertifications1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithCertifications2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithCertifications3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()
    }
    @Test
    fun `Search with alignments filter should apply to filtered search with AND operator between alignments`() {
        // Arrange
        val skillWithAlignments1and2 = TestObjectHelpers.randomRichSkillDoc().copy(alignments = listOf("alignment1", "alignment2"))
        val skillWithAlignments2and3 = TestObjectHelpers.randomRichSkillDoc().copy(alignments = listOf("alignment2", "alignment3"))
        val skillWithAlignments3and4 = TestObjectHelpers.randomRichSkillDoc().copy(alignments = listOf("alignment3", "alignment4"))

        richSkillEsRepo.saveAll(listOf(skillWithAlignments1and2,skillWithAlignments2and3,skillWithAlignments3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    alignments = listOf("alignment1", "alignment2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    alignments = listOf("alignment2", "alignment3")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    alignments = listOf("alignment3", "alignment4")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    alignments = listOf("alignment1", "alignment3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithAlignments1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithAlignments2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithAlignments3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()
    }
    @Test
    fun `Search with employers filter should apply to filtered search with AND operator between employers`() {
        // Arrange
        val skillWithEmployers1and2 = TestObjectHelpers.randomRichSkillDoc().copy(employers = listOf("employer1", "employer2"))
        val skillWithEmployers2and3 = TestObjectHelpers.randomRichSkillDoc().copy(employers = listOf("employer2", "employer3"))
        val skillWithEmployers3and4 = TestObjectHelpers.randomRichSkillDoc().copy(employers = listOf("employer3", "employer4"))

        richSkillEsRepo.saveAll(listOf(skillWithEmployers1and2,skillWithEmployers2and3,skillWithEmployers3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    employers = listOf("employer1", "employer2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    employers = listOf("employer2", "employer3")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    employers = listOf("employer3", "employer4")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    employers = listOf("employer1", "employer3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithEmployers1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithEmployers2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithEmployers3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()
    }
    @Test
    fun `search with occupations filter should apply to filtered search`() {
        // Arrange
        val jobCodes1and2 = listOf(
            TestObjectHelpers.randomJobCode().copy(code = "10-1111.11", id = TestObjectHelpers.elasticIdCounter),
            TestObjectHelpers.randomJobCode().copy(code = "10-2222.22", id = TestObjectHelpers.elasticIdCounter),
        )
        val jobCodes2and3 = listOf(
            TestObjectHelpers.randomJobCode().copy(code = "10-2222.22", id = TestObjectHelpers.elasticIdCounter),
            TestObjectHelpers.randomJobCode().copy(code = "10-3333.33", id = TestObjectHelpers.elasticIdCounter),
        )
        val jobCodes3and4 = listOf(
            TestObjectHelpers.randomJobCode().copy(code = "10-3333.33", id = TestObjectHelpers.elasticIdCounter),
            TestObjectHelpers.randomJobCode().copy(code = "10-4444.44", id = TestObjectHelpers.elasticIdCounter),
        )

        val skillWithOccupations1and2 = TestObjectHelpers.randomRichSkillDoc()
            .copy(jobCodes = jobCodes1and2)
        val skillWithOccupations2and3 = TestObjectHelpers.randomRichSkillDoc()
            .copy(jobCodes = jobCodes2and3)
        val skillWithOccupations3and4 = TestObjectHelpers.randomRichSkillDoc()
            .copy(jobCodes = jobCodes3and4)

        richSkillEsRepo.saveAll(listOf(skillWithOccupations1and2, skillWithOccupations2and3, skillWithOccupations3and4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        "10-11",
                        "10-22"
                    )
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        "10-22",
                        "10-33"
                    )
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        "10-33",
                        "10-44"
                    )
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                advanced = ApiAdvancedSearch(
                    occupations = listOf(
                        "10-11",
                        "10-33"
                    )
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithOccupations1and2.uuid)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithOccupations2and3.uuid)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skillWithOccupations3and4.uuid)
        assertThat(filteredSearchResult4).isEmpty()

    }
    @Test
    fun `search with authors filter should apply to filtered search`() {
        // Arrange
        val skillWithAuthor1 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author1"))
        val skillWithAuthor2 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author2"))
        val skillWithAuthor3 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author3"))

        richSkillEsRepo.saveAll(listOf(skillWithAuthor1,skillWithAuthor2,skillWithAuthor3))

        // Act
        val filteredSearchResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    authors = listOf("author1", "author3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult.searchHits.first().content.uuid).isEqualTo(skillWithAuthor1.uuid)
        assertThat(filteredSearchResult.searchHits[1].content.uuid).isEqualTo(skillWithAuthor3.uuid)
    }
    @Test
    fun `search with authors & categories filter should apply to filtered search with AND operator between fields, and OR operator between values`() {
        // Arrange
        val skillWithAuthor1AndCategory1 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author1"), categories = listOf("category1"))
        val skillWithAuthor2AndCategory2 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author2"), categories = listOf("category2"))
        val skillWithAuthor1AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author1"), categories = listOf("category3"))
        val skillWithAuthor2AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author2"), categories = listOf("category3"))
        val skillWithAuthor3AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(authors = listOf("author3"), categories = listOf("category3"))

        richSkillEsRepo.saveAll(listOf(skillWithAuthor1AndCategory1,skillWithAuthor2AndCategory2,skillWithAuthor3AndCategory3,
            skillWithAuthor1AndCategory3,skillWithAuthor2AndCategory3))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    authors = listOf("author1", "author2"),
                    categories = listOf("category1", "category2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    authors = listOf("author1", "author2", "author3"),
                    categories = listOf("category1", "category2", "category3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits).hasSize(2)
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skillWithAuthor1AndCategory1.uuid)
        assertThat(filteredSearchResult1.searchHits[1].content.uuid).isEqualTo(skillWithAuthor2AndCategory2.uuid)
        assertThat(filteredSearchResult2.searchHits).hasSize(5)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithAuthor1AndCategory1.uuid)
        assertThat(filteredSearchResult2.searchHits[4].content.uuid).isEqualTo(skillWithAuthor2AndCategory3.uuid)
    }
    @Test
    fun `search with authors & categories & keywords filter should apply to filtered search with AND operator between fields, and OR operator between categories and authors and AND between keywords`() {
        // Arrange
        val skillWithAuthor1AndCategory1 = TestObjectHelpers.randomRichSkillDoc().copy(
            authors = listOf("author1"),
            categories = listOf("category1"),
            searchingKeywords = listOf("keyword1")
        )
        val skillWithAuthor2AndCategory2 = TestObjectHelpers.randomRichSkillDoc().copy(
            authors = listOf("author2"),
            categories = listOf("category2"),
            searchingKeywords = listOf("keyword2")
        )
        val skillWithAuthor1AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(
            authors = listOf("author1"),
            categories = listOf("category3"),
            searchingKeywords = listOf("keyword1", "keyword3")
        )
        val skillWithAuthor2AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(
            authors = listOf("author2"),
            categories = listOf("category3"),
            searchingKeywords = listOf("keyword2", "keyword3")
        )
        val skillWithAuthor3AndCategory3 = TestObjectHelpers.randomRichSkillDoc().copy(
            authors = listOf("author3"),
            categories = listOf("category3"),
            searchingKeywords = listOf("keyword1", "keyword2", "keyword3")
        )

        richSkillEsRepo.saveAll(listOf(skillWithAuthor1AndCategory1,skillWithAuthor2AndCategory2,skillWithAuthor3AndCategory3,
            skillWithAuthor1AndCategory3,skillWithAuthor2AndCategory3))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    authors = listOf("author1", "author2"),
                    categories = listOf("category1", "category2"),
                    keywords = listOf("keyword1", "keyword2")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    authors = listOf("author1", "author2", "author3"),
                    categories = listOf("category1", "category2", "category3"),
                    keywords = listOf("keyword1", "keyword2", "keyword3")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits).isEmpty()
        assertThat(filteredSearchResult2.searchHits).hasSize(1)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skillWithAuthor3AndCategory3.uuid)
    }
    @Test
    fun `search with categories, keywords & standards filter should apply to filtered search with AND operator between fields, and OR operator between categories and AND operator between keywords and standards`() {
        // Arrange
        val skill1 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category1"),
            searchingKeywords = listOf("keyword1"),
            standards = listOf("standard1")
        )
        val skill2 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category2"),
            searchingKeywords = listOf("keyword1", "keyword2"),
            standards = listOf("standard1", "standard2")
        )
        val skill3 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category3"),
            searchingKeywords = listOf("keyword1", "keyword2", "keyword3"),
            standards = listOf("standard1", "standard2", "standard3")
        )
        val skill4 = TestObjectHelpers.randomRichSkillDoc().copy(categories = listOf("category4"),
            searchingKeywords = listOf("keyword1", "keyword2", "keyword3", "keyword4"),
            standards = listOf("standard1", "standard2", "standard3", "standard4")
        )


        richSkillEsRepo.saveAll(listOf(skill1,skill2,skill3,skill4))

        // Act
        val filteredSearchResult1 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category2"),
                    keywords = listOf("keyword1", "keyword2"),
                    standards = listOf("standard1")
                )
            )
        )
        val filteredSearchResult2 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category2", "category3"),
                    keywords = listOf("keyword1", "keyword2", "keyword3"),
                    standards = listOf("standard1", "standard2")
                )
            )
        )
        val filteredSearchResult3 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category2", "category3"),
                    keywords = listOf("keyword1", "keyword2", "keyword3"),
                    standards = listOf("standard1", "standard2", "standard3")
                )
            )
        )
        val filteredSearchResult4 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category2", "category3"),
                    keywords = listOf("keyword1", "keyword2", "keyword3"),
                    standards = listOf("standard1", "standard2", "standard3", "standard4")
                )
            )
        )
        val filteredSearchResult5 = richSkillEsRepo.byApiSearch(
            ApiSearch(
                filtered = ApiFilteredSearch(
                    categories = listOf("category1", "category2", "category3", "category4"),
                    keywords = listOf("keyword1"),
                    standards = listOf("standard1")
                )
            )
        )

        // Assert
        assertThat(filteredSearchResult1.searchHits).hasSize(1)
        assertThat(filteredSearchResult1.searchHits.first().content.uuid).isEqualTo(skill2.uuid)
        assertThat(filteredSearchResult2.searchHits).hasSize(1)
        assertThat(filteredSearchResult2.searchHits.first().content.uuid).isEqualTo(skill3.uuid)
        assertThat(filteredSearchResult3.searchHits).hasSize(1)
        assertThat(filteredSearchResult3.searchHits.first().content.uuid).isEqualTo(skill3.uuid)
        assertThat(filteredSearchResult4.searchHits).isEmpty()
        assertThat(filteredSearchResult5.searchHits).hasSize(4)
    }

    @Test
    fun testFindSimilar() {
        // Arrange
        val numOfSkills = 2L
        val skillByStatement1 = TestObjectHelpers.randomRichSkillDoc().copy(statement = "Evaluate the level of listening to avoid listening traps.")
        val skillByStatement2 = TestObjectHelpers.randomRichSkillDoc().copy(statement = "Evaluate the level of listening to understand the message being conveyed.")

        richSkillEsRepo.saveAll(listOf(skillByStatement1,skillByStatement2))

        // Act
        val result = richSkillEsRepo.findSimilar(ApiSimilaritySearch("Evaluate the level"))

        // Assert
        assertThat(result.totalHits).isEqualTo(numOfSkills)
        assertThat(result.searchHits.map { it.content.uuid }.toList()).contains(skillByStatement1.uuid)
    }

    @Test
    fun testFindByUuid() {
        // Arrange
        val skill = TestObjectHelpers.randomRichSkillDoc()

        richSkillEsRepo.saveAll(listOf(skill))

        // Act
        val result = richSkillEsRepo.findByUuid(skill.uuid)

        // Assert
        assertThat(result.first().uuid).contains(skill.uuid)
    }

    @Test
    fun testByApiSearchWithUuids() {
        // Arrange
        var collection1 = TestObjectHelpers.collectionDoc(name = "Self-Management Collection", description = "Collection of Self-Management Skills")
        val collection2 = TestObjectHelpers.collectionDoc(name = "Best Self Management Collection", description = "Collection of the best Self Management Skills")

        val skill1 = TestObjectHelpers.richSkillDoc(
            name = "Self-Management",
            statement = "A statement for a skill"
        ).copy(collections = listOf(collection1))
        val skill2 = TestObjectHelpers.richSkillDoc(
            name = "Self Mis-Management",
            statement = "A statement for a skill"
        )
        val skill3 = TestObjectHelpers.richSkillDoc(
            name = "Best Self Management",
            statement = "A statement for a skill"
        ).copy(collections = listOf(collection2))
        val skill4 = TestObjectHelpers.richSkillDoc(
            name = "Management of Selfies",
            statement = "A statement for a skill"
        )

        collection1 = collection1.copy(skillIds = listOf(skill1.uuid, skill2.uuid))

        val randomSkills = (1..10).map { TestObjectHelpers.randomRichSkillDoc() }
        richSkillEsRepo.saveAll(randomSkills + listOf(skill1, skill2, skill3, skill4))
        collectionEsRepo.saveAll(listOf(collection1, collection2))

        //Act
        val skillResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                uuids = listOf(randomSkills[0].uuid, skill1.uuid, skill2.uuid)
            ), collectionId = collection1.uuid
        ).searchHits.map { it.content }

        // Assert
        assertThat(skillResult.isNotEmpty())
        assertThat(skillResult.size).isEqualTo(1)
        assertThat(skillResult[0] == skill1)

    }

    @Test
    fun testByApiSearchWithUuidsAndNoCollection() {
        // Arrange
        var collection1 = TestObjectHelpers.collectionDoc(name = "Self-Management Collection", description = "Collection of Self-Management Skills")
        val collection2 = TestObjectHelpers.collectionDoc(name = "Best Self Management Collection", description = "Collection of the best Self Management Skills")

        val skill1 = TestObjectHelpers.richSkillDoc(
            name = "Self-Management",
            statement = "A statement for a skill"
        ).copy(collections = listOf(collection1))
        val skill2 = TestObjectHelpers.richSkillDoc(
            name = "Self Mis-Management",
            statement = "A statement for a skill"
        )
        val skill3 = TestObjectHelpers.richSkillDoc(
            name = "Best Self Management",
            statement = "A statement for a skill"
        ).copy(collections = listOf(collection2))
        val skill4 = TestObjectHelpers.richSkillDoc(
            name = "Management of Selfies",
            statement = "A statement for a skill"
        )

        collection1 = collection1.copy(skillIds = listOf(skill1.uuid, skill2.uuid))

        val randomSkills = (1..10).map { TestObjectHelpers.randomRichSkillDoc() }
        richSkillEsRepo.saveAll(randomSkills + listOf(skill1, skill2, skill3, skill4))
        collectionEsRepo.saveAll(listOf(collection1, collection2))

        //Act
        val skillResult = richSkillEsRepo.byApiSearch(
            ApiSearch(
                uuids = listOf(randomSkills[0].uuid, skill2.uuid)
            )
        ).searchHits.map { it.content }

        // Assert
        assertThat(skillResult.isNotEmpty())
        assertThat(skillResult.size).isEqualTo(2)
        assertThat(skillResult.contains(randomSkills[0]))
        assertThat(skillResult.contains(skill2))

    }
}
