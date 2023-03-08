package edu.wgu.osmt.keyword

import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.richskill.RichSkillEsRepo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class KeywordEsRepoTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo
) : SpringTest(), HasDatabaseReset, HasElasticsearchReset {

    @BeforeEach
    fun generateKeywordNoise(){
        KeywordTypeEnum.values().map{
            TestObjectHelpers.keywordsGenerator(50, it).also{ keywords ->
                keywordEsRepo.saveAll(keywords)
            }
        }
    }

    @Test
    fun `Should get results for 'keyword' type ahead searches `(){
        val testKeywords = listOf(
            TestObjectHelpers.keyword("Red", KeywordTypeEnum.Keyword),
            TestObjectHelpers.keyword("Redact", KeywordTypeEnum.Keyword),
            TestObjectHelpers.keyword("Orange", KeywordTypeEnum.Keyword),
            TestObjectHelpers.keyword("Yellow", KeywordTypeEnum.Keyword),
            TestObjectHelpers.keyword("Yelling", KeywordTypeEnum.Keyword),
            TestObjectHelpers.keyword("Yelp", KeywordTypeEnum.Keyword)
        )
        keywordEsRepo.saveAll(testKeywords)

        val results = keywordEsRepo.typeAheadSearch("red", KeywordTypeEnum.Keyword)
        val result2 = keywordEsRepo.typeAheadSearch("yEl", KeywordTypeEnum.Keyword)
        val result3 = keywordEsRepo.typeAheadSearch("yell", KeywordTypeEnum.Keyword)
        val result4 = keywordEsRepo.typeAheadSearch("yellow", KeywordTypeEnum.Keyword)
        val result5 = keywordEsRepo.typeAheadSearch("", KeywordTypeEnum.Keyword)


        assertThat(results.searchHits.count()).isEqualTo(2)
        assertThat(result2.searchHits.count()).isEqualTo(3)
        assertThat(result3.searchHits.count()).isEqualTo(2)
        assertThat(result4.searchHits.count()).isEqualTo(1)
        assertThat(result4.searchHits.first().content.value).isEqualTo("Yellow")
        assertThat(result5.searchHits).hasSize(56)
    }
}
