package edu.wgu.osmt.api.model

import edu.wgu.osmt.db.PublishStatus
import java.time.ZonedDateTime

interface IApiCollection {

    val id: String
    val uuid: String
    val name: String
    val description: String?
    val creator: String
    val author: String?
    val status: PublishStatus?
    val creationDate: ZonedDateTime
    val updateDate: ZonedDateTime
    val publishDate: ZonedDateTime?
    val archiveDate: ZonedDateTime?
    val skills: List<ApiSkillSummary>
    val owner: String?
}
