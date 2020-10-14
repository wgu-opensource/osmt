package edu.wgu.osmt.task

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.SpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

class TaskTest : SpringTest(), BaseDockerizedTest {

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
