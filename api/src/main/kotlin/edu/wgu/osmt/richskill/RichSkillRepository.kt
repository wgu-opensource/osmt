package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.ExposedCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.beans.factory.annotation.Autowired

interface RichSkillRepository : ExposedCrudRepository<RichSkillDescriptor, RsdUpdateObject>

@Repository
class RichSkillRepositoryImpl @Autowired constructor(override val table: RichSkillDescriptorTable) :
    RichSkillRepository {

}
