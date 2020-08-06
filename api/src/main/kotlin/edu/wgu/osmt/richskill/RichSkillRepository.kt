package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.ExposedCrudRepository
import edu.wgu.osmt.db.TableWithUpdateMapper
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Repository
import org.springframework.beans.factory.annotation.Autowired

interface RichSkillRepository : ExposedCrudRepository<RichSkillDescriptor, RsdUpdateObject>

@Repository
class RichSkillRepositoryImpl :
    RichSkillRepository {
    override val table: TableWithUpdateMapper<RichSkillDescriptor, RsdUpdateObject> = RichSkillDescriptorTable


    override suspend fun findAll(): List<RichSkillDescriptor> = newSuspendedTransaction() {
        table.leftJoin(RichSkillJobCodes).leftJoin(JobCodeTable)
            .selectAll().map {
                val richSkillDescriptor = table.fromRow(it)
                val maybeJobCode = JobCodeTable.maybeJobCodeInflator(it)
                richSkillDescriptor to maybeJobCode
            }
            .groupBy({ it.first }, { it.second })
            .map { (rsd, jc) -> rsd.copy(jobCodes = jc.filterNotNull()) }
    }
}
