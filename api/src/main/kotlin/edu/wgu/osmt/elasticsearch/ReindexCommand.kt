package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("reindex")
class ReindexCommand: CommandLineRunner {
    val logger: Logger = LoggerFactory.getLogger(ReindexCommand::class.java)

    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var richSkillEsRepo: RichSkillEsRepo

    @Autowired
    lateinit var collectionEsRepo: CollectionEsRepo

    @Autowired
    lateinit var jobCodeEsRepo: JobCodeEsRepo

    @Autowired
    lateinit var keywordEsRepo: KeywordEsRepo

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Autowired
    lateinit var jobCodeRepository: JobCodeRepository

    override fun run(vararg args: String?) {
        logger.info("Beginning re-index Elasticsearch")

        transaction {
            logger.info("Re-indexing Skills")
            richSkillRepository.findAll().map {
                richSkillEsRepo.save(RichSkillDoc.fromDao(it, appConfig))
            }

            logger.info("Re-indexing Collections")
            collectionRepository.findAll().map{
                collectionEsRepo.save(it.toDoc())
            }

            logger.info("Re-indexing Keywords")
            keywordRepository.findAll().map{
                keywordEsRepo.save(it.toModel())
            }

            logger.info("Re-indexing JobCodes")
            jobCodeRepository.findAll().map{
                jobCodeEsRepo.save(it.toModel())
            }
        }

        (applicationContext as ConfigurableApplicationContext).close()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ReindexCommand::class.java, *args)
}
