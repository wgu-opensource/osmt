package edu.wgu.osmt.richskill

import edu.wgu.osmt.db.DatabaseData

data class RichSkillDescriptor(val title: String, val description: String, override val id: Long?): DatabaseData<RichSkillDescriptor>(){

    override fun withId(id: Long): RichSkillDescriptor {
        return copy(id = id)
    }
}
