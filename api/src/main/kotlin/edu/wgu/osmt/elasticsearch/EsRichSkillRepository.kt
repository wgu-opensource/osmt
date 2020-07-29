package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EsRichSkillRepository: CrudRepository<RichSkillDescriptor, Int>
