package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired

interface RichSkillRepository: CrudRepository<RichSkillDescriptor>

@Repository
class RichSkillRepositoryImpl @Autowired constructor(override val table: RichSkillDescriptorTable): RichSkillRepository {

//    @Transactional
//    fun create(rsd: RichSkillDescriptor) {table.createStatement()}
}
