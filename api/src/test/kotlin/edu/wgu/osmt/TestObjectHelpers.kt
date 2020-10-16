package edu.wgu.osmt

import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDoc
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.streams.asSequence

object TestObjectHelpers {

    var elasticIdCounter: Long = 0
        get() {
            return field++
        }

    val authorString = "unit-test-author"

    fun collectionDoc(
        id: Long = elasticIdCounter,
        name: String,
        publishStatus: PublishStatus = PublishStatus.Unpublished,
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

    fun randomStrings() = (1..10).map { randomString() }

    fun randomCollectionDoc(): CollectionDoc {
        return collectionDoc(name = randomString(), author = randomString())
    }

    fun randomJobCode(): JobCode {
        return JobCode(elasticIdCounter, code = randomString(), creationDate = LocalDateTime.now(ZoneOffset.UTC))
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
        publishStatus: PublishStatus = PublishStatus.Unpublished
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

    fun keywordGenerator(n: Int, type: KeywordTypeEnum): List<Keyword> {
        val keywords = (0..n).toList().map {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val word = Random().ints(10, 0, chars.length).asSequence().map(chars::get).joinToString("").capitalize()
            Keyword(null, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), type, word, null)
        }
        return keywords
    }
}

