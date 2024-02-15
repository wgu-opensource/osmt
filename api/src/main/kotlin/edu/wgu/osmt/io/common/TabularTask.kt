package edu.wgu.osmt.io.common

import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.task.Task
import edu.wgu.osmt.task.TaskMessageService
import org.springframework.beans.factory.annotation.Autowired

abstract class TabularTask<T: Task> {
    @Autowired
    lateinit var taskMessageService: TaskMessageService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var appConfig: AppConfig

    abstract fun tabularSkillsInCollectionProcessor(task: T)

    abstract fun tabularSkillsInFullLibraryProcessor(task: T)
}
