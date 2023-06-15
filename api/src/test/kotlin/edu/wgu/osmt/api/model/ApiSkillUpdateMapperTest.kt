package edu.wgu.osmt.api.model

import edu.wgu.osmt.*
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
class ApiSkillUpdateMapperTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset {
    
    @Autowired
    lateinit var richSkillRepository: RichSkillRepository
    
    
    val userString = "unittestuser"
    
    val userEmail = "user@email.com"
    
    @Test
    fun `mapApiSkillUpdateV2ToApiSkillUpdate return an Add object with the value to store and the Remove object with the stored list but the one to store`() {
        // arrange
        val originalSkillUpdate = TestObjectHelpers.apiSkillUpdateGenerator()
        val originalSkillDao = richSkillRepository.createFromApi(
            listOf(originalSkillUpdate),
            userString,
            userEmail
        ).first()
        val apiSkillUpdateV2 = ApiSkillUpdateV2(
            skillName = originalSkillDao.name,
            author = "Author1",
            category = "Category1"
        )
        
        // act
        val storedAuthors = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Author }.map { it.value }.toList()
        val storedCategories = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Category }.map { it.value }.toList()
        val actual = ApiSkillUpdateMapper.mapApiSkillUpdateV2ToApiSkillUpdate(apiSkillUpdateV2, originalSkillDao.uuid, richSkillRepository)
        
        // assert
        assertThat(storedAuthors).containsAll(listOf("Author1", "Author2", "Author3"))
        assertThat(storedCategories).containsAll(listOf("Category1", "Category2", "Category3"))
        assertThat(actual.authors!!.add).contains("Author1").hasSize(1)
        assertThat(actual.authors!!.remove).containsAll(listOf("Author2","Author3")).hasSize(2)
        assertThat(actual.categories!!.add).contains("Category1").hasSize(1)
        assertThat(actual.categories!!.remove).containsAll(listOf("Category2","Category3")).hasSize(2)
    }
    
    @Test
    fun `mapApiSkillUpdateV2ToApiSkillUpdate Return ApiSkillUpdate with empty objects when value to store is null`() {
        // arrange
        val originalSkillUpdate = TestObjectHelpers.apiSkillUpdateGenerator()
        val originalSkillDao = richSkillRepository.createFromApi(
            listOf(originalSkillUpdate),
            userString,
            userEmail
        ).first()
        val apiSkillUpdateV2 = ApiSkillUpdateV2(
            skillName = originalSkillDao.name,
            author = null,
            category = null
        )
        
        // act
        val storedAuthors = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Author }.map { it.value }.toList()
        val storedCategories = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Category }.map { it.value }.toList()
        val actual = ApiSkillUpdateMapper.mapApiSkillUpdateV2ToApiSkillUpdate(apiSkillUpdateV2, originalSkillDao.uuid, richSkillRepository)
        
        // assert
        assertThat(storedAuthors).containsAll(listOf("Author1", "Author2", "Author3"))
        assertThat(storedCategories).containsAll(listOf("Category1", "Category2", "Category3"))
        assertThat(actual.authors!!.add).isEmpty()
        assertThat(actual.authors!!.remove).isEmpty()
        assertThat(actual.categories!!.add).isEmpty()
        assertThat(actual.categories!!.remove).isEmpty()
    }
    
    @Test
    fun `mapApiSkillUpdateV2ToApiSkillUpdate return an Add object with the value to store and the Remove object with the stored list`() {
        // arrange
        val originalSkillUpdate = TestObjectHelpers.apiSkillUpdateGenerator()
        val originalSkillDao = richSkillRepository.createFromApi(
            listOf(originalSkillUpdate),
            userString,
            userEmail
        ).first()
        val apiSkillUpdateV2 = ApiSkillUpdateV2(
            skillName = originalSkillDao.name,
            author = "Author4",
            category = "Category4"
        )
        
        // act
        val storedAuthors = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Author }.map { it.value }.toList()
        val storedCategories = originalSkillDao.keywords.filter { it.type == KeywordTypeEnum.Category }.map { it.value }.toList()
        val actual = ApiSkillUpdateMapper.mapApiSkillUpdateV2ToApiSkillUpdate(apiSkillUpdateV2, originalSkillDao.uuid, richSkillRepository)
        
        // assert
        assertThat(storedAuthors).containsAll(listOf("Author1", "Author2", "Author3"))
        assertThat(storedCategories).containsAll(listOf("Category1", "Category2", "Category3"))
        assertThat(actual.authors!!.add).contains("Author4")
        assertThat(actual.authors!!.remove).containsAll(listOf("Author1", "Author2", "Author3"))
        assertThat(actual.categories!!.add).contains("Category4")
        assertThat(actual.categories!!.remove).containsAll(listOf("Category1", "Category2", "Category3"))
    }
}