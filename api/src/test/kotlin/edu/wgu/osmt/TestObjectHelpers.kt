package edu.wgu.osmt

import edu.wgu.osmt.api.model.ApiAlignment
import edu.wgu.osmt.api.model.ApiAlignmentListUpdate
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import org.assertj.core.api.Assertions.assertThat
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
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
        description: String? = null,
        publishStatus: PublishStatus = PublishStatus.Draft,
        skillIds: List<String> = listOf(),
        author: String? = null
    ) = CollectionDoc(
        id = id,
        uuid = UUID.randomUUID().toString(),
        name = name,
        description = description,
        publishStatus = publishStatus,
        skillIds = skillIds,
        skillCount = skillIds.count(),
        author = author ?: authorString + "-collection",
        workspaceOwner = "owner@email.com"
    )

    fun randomString(): String = UUID.randomUUID().toString().replace("-", "")

    fun randomCode(): String {
        val twoRandomDigits = (1..2).map { (0..9).random() }.joinToString("")
        val fourRandomDigits = (1..4).map { (0..9).random() }.joinToString("")
        return when ((1..2).random()) {
            1 -> "$twoRandomDigits$fourRandomDigits"
            else -> "$twoRandomDigits$fourRandomDigits.$twoRandomDigits"
        }
    }

    fun randomStrings() = (1..10).map { randomString() }

    fun randomCollectionDoc(): CollectionDoc {
        return collectionDoc(name = randomString(), description = randomString(), author = randomString())
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
            categories = listOf(randomString()),
            authors = listOf(randomString())
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
        categories: List<String> = listOf("default category"),
        authors: List<String> = listOf(authorString),
        publishStatus: PublishStatus = PublishStatus.Draft
    ): RichSkillDoc {
        val uuid = UUID.randomUUID().toString()
        return RichSkillDoc(
            id = id,
            uuid = uuid,
            uri = "/base/url/${uuid}",
            name = name,
            statement = statement,
            categories = categories,
            authors = authors,
            publishStatus = publishStatus,
            collections = listOf(collectionDoc(elasticIdCounter, UUID.randomUUID().toString(), randomString()))
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
        val keywords = (1..n).toList().map {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val word = Random().ints(5, 0, chars.length)
                .asSequence()
                .map(chars::get)
                .joinToString("")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            keyword(word, type)
        }
        return keywords
    }

    fun namedReferenceGenerator(includeName: Boolean = true, includeUri: Boolean = true): ApiNamedReference {
        val name = if (includeName) UUID.randomUUID().toString() else null
        val uri = if (includeUri) UUID.randomUUID().toString() else null
        return ApiNamedReference(id = uri, name = name)
    }
    fun alignmentGenerator(includeName: Boolean = true, includeUri: Boolean = true, frameworkName: String? = null): ApiAlignment {
        val name = if (includeName) UUID.randomUUID().toString() else null
        val uri = if (includeUri) UUID.randomUUID().toString() else null
        return ApiAlignment.fromStrings(id = uri, skillName = name, frameworkName = frameworkName)
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

        val authors = ApiStringListUpdate(
            add = (1..keywordCount).toList().map { "Author${it}" }
        )
        val categories = ApiStringListUpdate(
            add = (1..keywordCount).toList().map { "Category${it}" }
        )
        val keywords = ApiStringListUpdate(
            add = (1..keywordCount).toList().map { UUID.randomUUID().toString() }
        )
        val certifications = ApiReferenceListUpdate(
            add = (1..certificationCount).toList()
                .map { namedReferenceGenerator(includeName = false, includeUri = true) }
        )
        val standards = ApiAlignmentListUpdate(
            add = (1..standardCount).toList().map { alignmentGenerator(includeName = true, includeUri = true) }
        )
        val alignments = ApiAlignmentListUpdate(
            add = (1..alignmentCount).toList().map { alignmentGenerator(includeName = false, includeUri = true) }
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
            categories = categories,
            authors = authors,
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

    fun assertThatKeywordMatchesAlignment(keyword: Keyword?, alignment: ApiAlignment?) {
        assertThat(keyword).isNotNull
        assertThat(keyword?.uri).isEqualTo(alignment?.id)
        assertThat(keyword?.value).isEqualTo(alignment?.skillName)
    }
}
