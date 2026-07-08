package edu.wgu.osmt.credentialengine

import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.richskill.RichSkillDescriptor

interface SyncTarget {
    fun publishSkill(rsd: RichSkillDescriptor): Result<Unit>

    fun publishCollection(
        collection: Collection,
        skillCtids: List<String>,
    ): Result<Unit>

    fun deprecateSkill(rsd: RichSkillDescriptor): Result<Unit>

    fun deprecateCollection(collection: Collection): Result<Unit>

    /**
     * Deletes the given records from the target. Collections first, then skills.
     * For CE: calls delete API per CTID. For mock: clears in-memory state.
     */
    fun unpublishAll(
        collectionUuids: List<String>,
        skillUuids: List<String>,
    ): Result<Unit>
}
