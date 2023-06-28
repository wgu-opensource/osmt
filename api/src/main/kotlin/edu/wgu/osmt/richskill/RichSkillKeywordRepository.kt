package edu.wgu.osmt.richskill

import edu.wgu.osmt.keyword.KeywordDao
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface RichSkillKeywordRepository {
    val table: Table

    fun hasRSDsRelated(keywordDao: KeywordDao): Boolean
}

@Repository
@Transactional
class RichSkillKeywordRepositoryImpl: RichSkillKeywordRepository {
    override val table = RichSkillKeywords

    override fun hasRSDsRelated(keywordDao: KeywordDao): Boolean {
        return !table.select { table.keywordId eq keywordDao.id }.empty()
    }
}