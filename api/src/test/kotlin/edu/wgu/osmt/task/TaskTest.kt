package edu.wgu.osmt.task

import edu.wgu.osmt.ApplicationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TaskTest : ApplicationTest() {

    @Autowired
    lateinit var tms: TaskMessageService

    @Test
    fun `can serialize and deserialize a task into task hash table`() {
        val csvTask = CsvTask()
        tms.opsForHash.put(TaskMessageService.taskHashTable, csvTask.uuid, csvTask)
        val retrieved = tms.opsForHash.get(TaskMessageService.taskHashTable, csvTask.uuid)
        assertThat(csvTask.uuid).isEqualTo(retrieved?.uuid)
    }
}
