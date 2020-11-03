package edu.wgu.osmt

import edu.wgu.osmt.auditlog.AuditLogTable
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillJobCodes
import edu.wgu.osmt.richskill.RichSkillKeywords
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations=["classpath:config/application-test.properties"])
abstract class SpringTest: BaseDockerizedTest {
    val logger: Logger = LoggerFactory.getLogger(SpringTest::class.java)

    val testUser = "test-user"

    init {
        logger.info("Mysql port ${Containers.mysqlContainer.getMappedPort(3306)}")
        logger.info("Redis port ${Containers.redisContainer.getMappedPort(6379)}")
        logger.info("ES port ${Containers.elasticContainer.getMappedPort(9200)}")
    }
}


interface HasDatabaseReset {

    @Transactional
    @BeforeEach
    fun resetDb(): Unit {
        RichSkillJobCodes.deleteAll()
        RichSkillKeywords.deleteAll()
        CollectionSkills.deleteAll()
        RichSkillDescriptorTable.deleteAll()
        KeywordTable.deleteAll()
        CollectionTable.deleteAll()
        AuditLogTable.deleteAll()
    }
}

