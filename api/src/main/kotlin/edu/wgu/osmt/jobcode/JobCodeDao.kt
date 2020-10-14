package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.OutputsModel
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class JobCodeDao(id: EntityID<Long>) : LongEntity(id), OutputsModel<JobCode> {

    companion object : LongEntityClass<JobCodeDao>(JobCodeTable)

    var creationDate by JobCodeTable.creationDate

    var major by JobCodeTable.major
    var minor by JobCodeTable.minor
    var broad by JobCodeTable.broad
    var detailed by JobCodeTable.detailed

    var code by JobCodeTable.code
    var name by JobCodeTable.name
    var description by JobCodeTable.description
    var framework by JobCodeTable.framework
    var url by JobCodeTable.url


    override fun toModel(): JobCode =
        JobCode(
            id = id.value,
            creationDate = creationDate,
            major = major,
            minor = minor,
            broad = broad,
            detailed = detailed,
            code = code,
            name = name,
            description = description,
            framework = framework,
            url = url
        )

    fun toDoc(): JobCodeDoc = JobCodeDoc(
        major = major, minor = minor, broad = broad, detailed = detailed, code = code, name = name
    )
}
