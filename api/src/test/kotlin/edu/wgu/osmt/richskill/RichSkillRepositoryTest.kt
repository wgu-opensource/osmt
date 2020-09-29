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
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RichSkillRepositoryTest: BaseDockerizedTest() {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Test
    fun `should update an existing skill`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val created = richSkillRepository.create(name = name, statement = statement, author = null, user = "test", category = null).toModel()
        assertThat(created.name).isEqualTo(name)

        val newName = UUID.randomUUID().toString()
        val newStatement = UUID.randomUUID().toString()

        val updated = richSkillRepository.update(RsdUpdateObject(
            id=created.id!!,
            name=newName,
            statement=newStatement
        ), "test")
        val retrieved  = richSkillRepository.findById(created.id!!)

        assertThat(retrieved?.name).isEqualTo(newName)
        assertThat(updated?.name).isEqualTo(newName)

    }

    @Test
    fun `should insert and retrieve a rich skill to the database`(){
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val created = richSkillRepository.create(name = name, statement = statement, author = null, user = "test", category = null).toModel()
        val retrieved  = richSkillRepository.findById(created.id!!)

        assertThat(created.id!!).isEqualTo(retrieved?.id?.value)
        assertThat(retrieved?.name).isEqualTo(name)
        assertThat(retrieved?.statement).isEqualTo(statement)
    }
}
