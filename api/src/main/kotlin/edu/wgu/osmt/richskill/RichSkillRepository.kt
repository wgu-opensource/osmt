package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.BaseRepository
import edu.wgu.osmt.db.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.beans.factory.annotation.Autowired

interface RichSkillRepository: CrudRepository<RichSkillDescriptor, RsdUpdateObject>

@Repository
class RichSkillRepositoryImpl @Autowired constructor(override val table: RichSkillDescriptorTable): RichSkillRepository {

}
