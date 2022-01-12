package edu.wgu.osmt.collection

import edu.wgu.osmt.db.PublishStatusUpdate
import edu.wgu.osmt.db.TableWithUpdate
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

object CollectionTable: TableWithUpdate<CollectionUpdateObject>, PublishStatusUpdate<CollectionUpdateObject>, LongIdTable("Collection") {
    override val creationDate = datetime("creationDate")
    override val updateDate = datetime("updateDate")
    override val archiveDate: Column<LocalDateTime?> = datetime("archiveDate").nullable()
    override val publishDate: Column<LocalDateTime?> = datetime("publishDate").nullable()
    val uuid = varchar("uuid", 36).uniqueIndex()
    val name = text("name")
    val author = reference(
        "author_id",
        KeywordTable,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.CASCADE
    ).nullable()


}

object CollectionSkills : Table("CollectionSkills") {
    val collectionId = reference(
        "collection_id",
        CollectionTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    val skillId = reference(
        "skill_id",
        RichSkillDescriptorTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
    override val primaryKey = PrimaryKey(collectionId, skillId, name = "PK_CollectionSkills_c_rs")

    fun create(collectionId: Long, skillId: Long) {
        insertIgnore {
            it[this.collectionId] = EntityID(collectionId, CollectionTable)
            it[this.skillId] = EntityID(skillId, RichSkillDescriptorTable)
        }
    }
    fun delete(collectionId: Long, skillId: Long): Int {
        return deleteWhere {
            (CollectionSkills.collectionId eq collectionId) and
            (CollectionSkills.skillId eq skillId)
        }
    }
}
