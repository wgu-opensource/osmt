package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.util.*

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RichSkillRepositoryTest: BaseDockerizedTest() {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Test
    fun `should insert and retrieve a rich skill to the database`(){
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val created = transaction { richSkillRepository.create(name = name, statement = statement, author = null, user = null).toModel() }
        val retrieved  = transaction { richSkillRepository.findById(created.id!!) }

        assertThat(created.id).isEqualTo(retrieved?.id)
        assertThat(retrieved?.name).isEqualTo(name)
        assertThat(retrieved?.statement).isEqualTo(statement)
    }
}
