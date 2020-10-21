package edu.wgu.osmt.richskill

import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao

// Convenience class for holding a RichSkillDescriptor and associated Collections
data class RichSkillAndCollections(val rs: RichSkillDescriptor, val collections: Set<Collection>) {

    companion object {
        fun fromDao(rsDao: RichSkillDescriptorDao): RichSkillAndCollections {
            return RichSkillAndCollections(rsDao.toModel(), rsDao.collections.map { it.toModel() }.toSet())
        }
    }
}

data class CollectionAndRichSkills(val collection: Collection, val skills: Set<RichSkillDescriptor>) {
    companion object {
        fun fromDao(collectionDao: CollectionDao): CollectionAndRichSkills {
            return CollectionAndRichSkills(collectionDao.toModel(), collectionDao.skills.map { it.toModel() }.toSet())
        }
    }
}