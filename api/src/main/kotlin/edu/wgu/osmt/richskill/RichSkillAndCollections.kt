package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.Collection

// Convenience class for holding a RichSkillDescriptor and associated Collections
data class RichSkillAndCollections(val rs: RichSkillDescriptor, val collections: Set<Collection>) {

    companion object {
        fun fromDao(rsDao: RichSkillDescriptorDao): RichSkillAndCollections {
            return RichSkillAndCollections(rsDao.toModel(), rsDao.collections.map { it.toModel() }.toSet())
        }
    }
}
