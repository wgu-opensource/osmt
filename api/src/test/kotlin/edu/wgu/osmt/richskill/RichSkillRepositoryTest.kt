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

    val userString = "unittestuser"

    @Test
    fun `should not create a rich skill without a non-blank name and statement`(){
        assertThat(richSkillRepository.create(RsdUpdateObject(), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(name="name"), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(statement="statement"), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(name=" ",statement=" "), userString)).isNull()
    }

    @Test
    fun `should insert and retrieve a rich skill to the database`(){
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val updateObject = RsdUpdateObject(name = name, statement = statement)

        val created = richSkillRepository.create(updateObject, userString)
        assertThat(created).isNotNull

        val retrieved  = richSkillRepository.findById(created!!.id.value)
        assertThat(created.id.value).isEqualTo(retrieved?.id?.value)
        assertThat(retrieved?.name).isEqualTo(name)
        assertThat(retrieved?.statement).isEqualTo(statement)
    }
}
