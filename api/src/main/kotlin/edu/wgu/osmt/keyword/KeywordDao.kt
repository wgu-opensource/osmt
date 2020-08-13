package edu.wgu.osmt.keyword

import edu.wgu.osmt.db.OutputsModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class KeywordDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<Keyword> {
    companion object : LongEntityClass<KeywordDao>(KeywordTable)

    var creationDate by KeywordTable.creationDate
    var updateDate by KeywordTable.updateDate

    var value by KeywordTable.value
    var type by KeywordTable.keyword_type_enum
    var uri by KeywordTable.uri

    override fun toModel(): Keyword = Keyword(
        id = id.value,
        creationDate = creationDate,
        updateDate = updateDate,
        value = value,
        type = type,
        uri = uri
    )
}
