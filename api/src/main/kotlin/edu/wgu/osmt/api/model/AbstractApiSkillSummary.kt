package edu.wgu.osmt.api.model

import edu.wgu.osmt.db.PublishStatus
import java.time.LocalDateTime

abstract class AbstractApiSkillSummary {

    abstract val id: String
    abstract val uuid: String
    abstract val status: PublishStatus
    abstract val publishDate: LocalDateTime?
    abstract val archiveDate: LocalDateTime?
    abstract val skillName: String
    abstract val skillStatement: String
    abstract val keywords: List<String>
    abstract val occupations: List<ApiJobCode>

}
