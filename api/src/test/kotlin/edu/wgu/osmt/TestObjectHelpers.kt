package edu.wgu.osmt

import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import org.assertj.core.api.Assertions.assertThat
import edu.wgu.osmt.richskill.RichSkillDoc
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.math.abs
import kotlin.streams.asSequence

object TestObjectHelpers {

    val secureRandom = SecureRandom()

    var elasticIdCounter: Long = 0
        get() {
            return field++
        }

    val authorString = "unit-test-author"

    fun collectionDoc(
        id: Long = elasticIdCounter,
        name: String,
        publishStatus: PublishStatus = PublishStatus.Draft,
        skillIds: List<String> = listOf(),
        author: String? = null
    ) = CollectionDoc(
        id = id,
        uuid = UUID.randomUUID().toString(),
        name = name,
        publishStatus = publishStatus,
        skillIds = skillIds,
        skillCount = skillIds.count(),
        author = author ?: authorString + "-collection"
    )

    fun randomString(): String = UUID.randomUUID().toString().replace("-", "")

    fun randomCode(): String {
        val twoRandomDigits = (1..2).map { (0..9).random() }.joinToString("")
        val fourRandomDigits = (1..4).map { (0..9).random() }.joinToString("")
        return when ((1..2).random()) {
            1 -> "$twoRandomDigits-$fourRandomDigits"
            else -> "$twoRandomDigits-$fourRandomDigits.$twoRandomDigits"
        }
    }

    fun randomStrings() = (1..10).map { randomString() }

    fun randomCollectionDoc(): CollectionDoc {
        return collectionDoc(name = randomString(), author = randomString())
    }

    fun randomJobCode(): JobCode {
        return JobCode(
            elasticIdCounter,
            code = randomCode(),
            name = randomString(),
            creationDate = LocalDateTime.now(ZoneOffset.UTC)
        )
    }

    fun randomRichSkillDoc(): RichSkillDoc {
        return richSkillDoc(
            name = randomString(),
            statement = randomString(),
            category = randomString(),
            author = randomString()
        ).copy(
            searchingKeywords = randomStrings(),
            standards = randomStrings(),
            certifications = randomStrings(),
            alignments = randomStrings(),
            employers = randomStrings(),
            jobCodes = (1..10).map { randomJobCode() },
            collections = (1..5).map { randomCollectionDoc() }
        )
    }

    fun richSkillDoc(
        id: Long = elasticIdCounter,
        name: String,
        statement: String,
        category: String? = "default category",
        author: String = authorString,
        publishStatus: PublishStatus = PublishStatus.Draft
    ): RichSkillDoc {
        val uuid = UUID.randomUUID().toString()
        return RichSkillDoc(
            id = id,
            uuid = uuid,
            uri = "/base/url/${uuid}",
            name = name,
            statement = statement,
            category = category,
            author = author,
            publishStatus = publishStatus,
            collections = listOf(collectionDoc(elasticIdCounter, UUID.randomUUID().toString()))
        )
    }

    fun keyword(value: String, type: KeywordTypeEnum): Keyword {
        return Keyword(
            elasticIdCounter,
            LocalDateTime.now(ZoneOffset.UTC),
            LocalDateTime.now(ZoneOffset.UTC),
            type,
            value,
            null
        )
    }

    fun keywordsGenerator(n: Int, type: KeywordTypeEnum): List<Keyword> {
        val keywords = (0..n).toList().map {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val word = Random().ints(10, 0, chars.length).asSequence().map(chars::get).joinToString("").capitalize()
            keyword(word, type)
        }
        return keywords
    }

    fun namedReferenceGenerator(includeName: Boolean = true, includeUri: Boolean = true): ApiNamedReference {
        val name = if (includeName) UUID.randomUUID().toString() else null
        val uri = if (includeUri) UUID.randomUUID().toString() else null
        return ApiNamedReference(id = uri, name = name)
    }

    fun apiSkillUpdateGenerator(
        name: String? = null,
        statement: String? = null,
        publishStatus: PublishStatus = PublishStatus.Draft,
        keywordCount: Int = 3,
        certificationCount: Int = 3,
        standardCount: Int = 3,
        alignmentCount: Int = 3,
        employerCount: Int = 3,
        occupationCount: Int = 3,
        collectionCount: Int = 3
    ): ApiSkillUpdate {
        val skillName = name ?: UUID.randomUUID().toString()
        val skillStatement = statement ?: UUID.randomUUID().toString()
        val categoryName = UUID.randomUUID().toString()
        val author = namedReferenceGenerator(includeName = true, includeUri = false)

        val keywords = ApiStringListUpdate(
            add = (1..keywordCount).toList().map { UUID.randomUUID().toString() }
        )
        val certifications = ApiReferenceListUpdate(
            add = (1..certificationCount).toList()
                .map { namedReferenceGenerator(includeName = false, includeUri = true) }
        )
        val standards = ApiReferenceListUpdate(
            add = (1..standardCount).toList().map { namedReferenceGenerator(includeName = true, includeUri = true) }
        )
        val alignments = ApiReferenceListUpdate(
            add = (1..alignmentCount).toList().map { namedReferenceGenerator(includeName = false, includeUri = true) }
        )
        val employers = ApiReferenceListUpdate(
            add = (1..employerCount).toList().map { namedReferenceGenerator(includeName = true, includeUri = false) }
        )
        val occupations = ApiStringListUpdate(
            add = (1..occupationCount).toList().map { UUID.randomUUID().toString() }
        )
        val collections = ApiStringListUpdate(
            add = (1..collectionCount).toList().map { UUID.randomUUID().toString() }
        )

        return ApiSkillUpdate(
            skillName = skillName,
            skillStatement = skillStatement,
            publishStatus = publishStatus,
            category = categoryName,
            author = author,
            keywords = keywords,
            certifications = certifications,
            standards = standards,
            alignments = alignments,
            employers = employers,
            occupations = occupations,
            collections = collections
        )
    }

    fun assertThatKeywordMatchesNamedReference(keyword: Keyword?, namedReference: ApiNamedReference?) {
        assertThat(keyword).isNotNull
        assertThat(keyword?.uri).isEqualTo(namedReference?.id)
        assertThat(keyword?.value).isEqualTo(namedReference?.name)
    }
}
