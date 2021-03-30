package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.osmt.richskill.RichSkillEsRepo
import edu.wgu.osmt.richskill.RichSkillRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ElasticSearchReindexer {
    val logger: Logger = LoggerFactory.getLogger(ElasticSearchReindexer::class.java)

    @Autowired
    lateinit var appConfig: AppConfig

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

    @Value("\${edu.wgu.osmt.elasticsearch.Reindex.batch_size:1000}")
    lateinit var limit: Integer

    fun reindexAll() {
        reimportSkills()
        reimportCollections()
        reimportKeywords()
        reimportJobCodes()
    }

    private fun reimportSkills() {
        var page = 0
        var exit = false
        val trace = TraceProcessing("skills", logger)
        while (!exit) {
            transaction {
                val results = richSkillRepository.findAll()
                val offset = (page++ * limit.toInt()).toLong()

                val subSet = results.limit(limit.toInt(), offset)
                trace.start(results.count(), offset, limit.toInt())
                subSet.map {
                    richSkillEsRepo.save(RichSkillDoc.fromDao(it, appConfig))
                    trace.increment(it.uuid, it.name)
                }

                if (results.count() <= trace.offset()) {
                    trace.finish()
                    exit = true
                }
            }
        }
    }

    private fun reimportCollections() {
        var page = 0
        var exit = false
        var trace = TraceProcessing("collections", logger)
        while (!exit) {
            transaction {
                var results = collectionRepository.findAll()
                val offset = (page++ * limit.toInt()).toLong()

                val subSet = results.limit(limit.toInt(), offset)
                trace.start(results.count(), offset, limit.toInt())
                subSet.map {
                    collectionEsRepo.save(it.toDoc())
                    trace.increment(it.uuid, it.name)
                }

                if (results.count() <= trace.offset()) {
                    trace.finish()
                    exit = true
                }
            }
        }
    }

    private fun reimportKeywords() {
        var page = 0
        var exit = false
        var trace = TraceProcessing("keywords", logger)
        while (!exit) {
            transaction {
                var results = keywordRepository.findAll()
                val offset = (page++ * limit.toInt()).toLong()

                val subSet = results.limit(limit.toInt(), offset)
                trace.start(results.count(), offset, limit.toInt())
                subSet.map {
                    keywordEsRepo.save(it.toModel())
                    trace.increment(it.type.displayName, it.value)
                }

                if (results.count() <= trace.offset()) {
                    trace.finish()
                    exit = true
                }
            }
        }
    }

    private fun reimportJobCodes() {
        var page = 0
        var exit = false
        var trace = TraceProcessing("jobCodes", logger)
        while (!exit) {
            transaction {
                var results = jobCodeRepository.findAll()
                val offset = (page++ * limit.toInt()).toLong()

                val subSet = results.limit(limit.toInt(), offset)
                trace.start(results.count(), offset, limit.toInt())
                subSet.map {
                    jobCodeEsRepo.save(it.toModel())
                    trace.increment(it.code, it.name)
                }

                if (results.count() <= trace.offset()) {
                    trace.finish()
                    exit = true
                }

            }
        }
    }
}
