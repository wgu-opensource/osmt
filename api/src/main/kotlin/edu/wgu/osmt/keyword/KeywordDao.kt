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
    var type by KeywordTypeDao referencedOn KeywordTable.keyword_type

    override fun toModel(): Keyword = Keyword(
        id = id.value,
        creationDate = creationDate,
        updateDate = updateDate,
        value = value,
        type = type.toModel()
    )
}

class KeywordTypeDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<KeywordType> {
    companion object : LongEntityClass<KeywordTypeDao>(KeywordTypeTable)

    var creationDate by KeywordTypeTable.creationDate
    var updateDate by KeywordTypeTable.updateDate

    var type: String by KeywordTypeTable.type

    override fun toModel(): KeywordType = KeywordType(
        id = id.value,
        creationDate = creationDate,
        updateDate = updateDate,
        type = type
    )
}
