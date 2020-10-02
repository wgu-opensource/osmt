package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

interface EsRichSkillRepository : CrudRepository<RichSkillDescriptor, Int> {

    @Query("{\"match\": {\"uuid\": \"?0\"}}")
    fun findByUUID(uuid: String, pageable: Pageable): Page<RichSkillDescriptor>
}
