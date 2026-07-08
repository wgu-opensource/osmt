package edu.wgu.osmt.task

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class TaskResultExportXlsxTest {
    @Test
    fun `ExportSkillsToXlsxTask result uri uses media endpoint`() {
        val uuid = UUID.randomUUID().toString()
        val task = ExportSkillsToXlsxTask(uuids = listOf("skill-uuid"), uuid = uuid)
        val result = TaskResult.fromTask(task)

        assertThat(result.resultUri).isEqualTo("/api/v3/results/media/$uuid")
        assertThat(result.resultUri).contains("/results/media/")
    }
}
