package edu.wgu.osmt.task

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TaskTest : SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var tms: TaskMessageService

    fun assertTaskSerializes(task: Task) {
        tms.opsForHash.put(TaskMessageService.taskHashTable, task.uuid, task)
        val retrieved = tms.opsForHash.get(TaskMessageService.taskHashTable, task.uuid)
        assertThat(task.uuid).isEqualTo(retrieved?.uuid)
    }

    @Test
    fun `can serialize and deserialize a CsvTask`() {
        val csvTask = CsvTask()
        assertTaskSerializes(csvTask)
    }

    @Test
    fun `can serialize and deserialize a PublishSkillsTask`() {
        val task = PublishSkillsTask()
        assertTaskSerializes(task)
    }

    @Test
    fun `can serialize and deserialize a UpdateCollectionSkillsTask`() {
        val task = UpdateCollectionSkillsTask()
        assertTaskSerializes(task)
    }
}
