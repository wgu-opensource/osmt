package edu.wgu.osmt.mockdata

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import edu.wgu.osmt.collection.CollectionDoc
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.csv.BlsJobCode
import edu.wgu.osmt.csv.OnetJobCode
import edu.wgu.osmt.csv.RichSkillRow
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDoc
import edu.wgu.skills.service.mockdata.xml.Collection
import edu.wgu.skills.service.mockdata.xml.OsmtData
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
import javax.xml.stream.XMLStreamException

class MockData {
    private val log = LoggerFactory.getLogger(MockData::class.java)
    private val keywords: MutableMap<Long?, Keyword> = HashMap()
    private val collections: MutableMap<Long?, CollectionDoc> = HashMap()
    private val jobCodes: MutableMap<Long?, JobCode> = HashMap()
    private val richSkillDescriptor: MutableMap<String?, RichSkillDescriptor> = HashMap()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnn")

    var appConfig: AppConfig
        get() = field
    /**
     * The mock-data.xml file was generated by executing the SQL script defined in
     * tests/generate-integrated-test-data.sql
     */
    init {
        appConfig = createAppConfig()

        try {
            val filename = "mock-data.xml"
            val file = this::class.java.classLoader.getResource(filename)
            if (file == null) {
                log.error("Unable to open {}", filename)
            } else {
                val xml = file.readBytes()
                val xmlMapper = XmlMapper() //ObjectMapper().registerKotlinModule()
                val osmtData: OsmtData = xmlMapper.readValue(xml, OsmtData::class.java)
                populateKeywords(osmtData)
                populateJobCodes(osmtData)
                populateCollections(osmtData)
                populateRichSkillDescriptor(osmtData)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XMLStreamException) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieve all RichSkillDoc
     * @return List of [RichSkillDoc]
     */
    fun getRichSkillDocs(): List<RichSkillDoc> {
        return richSkillDescriptor.values.stream()
            .map{ rsd -> richSkillDescriptor2RichSkillDoc(rsd, appConfig)}
            .collect(Collectors.toList())
    }

    fun getRichSkillDescriptors(): List<RichSkillDescriptor> {
        return richSkillDescriptor.values.toList()
    }

    fun getCollectionDocs(): List<CollectionDoc> {
        return collections.values.stream().collect(Collectors.toList())
    }

    fun getJobCodes(): List<JobCode> {
        return jobCodes.values.stream().collect(Collectors.toList())
    }

    fun getKeywords(): List<Keyword> {
        return keywords.values.stream().collect(Collectors.toList())
    }

    fun getCollections(): List<edu.wgu.osmt.collection.Collection> {
        return collections.values.stream()
            .map { cd -> collectionDoc2Collection(cd)}
            .collect(Collectors.toList())
    }

    fun getCollection(id: Long) : edu.wgu.osmt.collection.Collection? {
        return collections[id]?.let { collectionDoc2Collection(it) }
    }

    /**
     * Retrieve a RichSkillDoc by its skillId (UUID)
     * @param skillId String containing the skillid
     * @return [RichSkillDoc]
     */
    fun getRichSkillDoc(skillId: String?): RichSkillDoc {
        return richSkillDescriptor2RichSkillDoc(richSkillDescriptor[skillId], appConfig)
    }

    /**
     * Retrieve a CollectionDoc by its collectionId (UUID)
     * @param collectionId String containing the collectionId
     * @return [CollectionDoc]
     */
    fun getCollectionDoc(collectionId: String): CollectionDoc? {
        val ret = collections.values.stream().filter { (_, uuid) -> uuid == collectionId }
            .findAny()
        return if (ret.isPresent) {
            ret.get()
        } else null
    }

    fun getRichSkillRows(): List<RichSkillRow> {
        return richSkillDescriptor.values.stream()
            .map{ rsd -> richSkillDescriptor2RichSkillRow(rsd)}
            .collect(Collectors.toList())
    }

    fun getBlsJobCodes(): List<BlsJobCode> {
        return jobCodes.values.stream()
                .map{ bls -> jobCode2BlsJobCode(bls)}
                .collect(Collectors.toList()).filter { it.code !=null }
    }

    fun getOnetJobCodes(): List<OnetJobCode> {
        return jobCodes.values.stream()
                .map{ onet -> jobCode2OnetJobCode(onet)}
                .collect(Collectors.toList()).filter { it.code !=null }
    }

    fun richSkillDescriptor2RichSkillDoc(rsd: RichSkillDescriptor?, appConfig: AppConfig): RichSkillDoc {
        return RichSkillDoc(
            id = rsd?.id!!,
            uuid = rsd.uuid,
            uri = "${appConfig.baseUrl}/api/skills/${rsd.uuid}",
            name = rsd.name,
            statement = rsd.statement,
            category = rsd.category?.value,
            author = rsd.author?.value,
            publishStatus = rsd.publishStatus(),
            searchingKeywords = rsd.searchingKeywords.mapNotNull { r -> r.value },
            jobCodes = rsd.jobCodes,
            standards = rsd.keywords.filter { it.type == KeywordTypeEnum.Standard }.mapNotNull { it.value },
            certifications = rsd.keywords.filter { it.type == KeywordTypeEnum.Certification }
                .mapNotNull { it.value },
            employers = rsd.keywords.filter { it.type == KeywordTypeEnum.Employer }.mapNotNull { it.value },
            alignments = rsd.keywords.filter { it.type == KeywordTypeEnum.Alignment }.mapNotNull { it.value },
            collections = rsd.collections.map { collection2CollectionDoc(it) },
            publishDate = rsd.publishDate,
            archiveDate = rsd.archiveDate
        )
    }

    private fun richSkillDescriptor2RichSkillRow(
        rsd: RichSkillDescriptor?
    ): RichSkillRow {
        val sep = "; "
        val richSkillRow = RichSkillRow()
        richSkillRow.collections = rsd?.collections?.map{ it.name }?.joinToString(separator = sep)
        richSkillRow.skillName = rsd?.name
        richSkillRow.skillCategory = rsd?.category?.value
        richSkillRow.skillStatement = rsd?.statement
        richSkillRow.keywords = rsd?.keywords?.map{ it.value }?.joinToString(separator = sep)
        richSkillRow.standards = rsd?.standards?.map{ it.value }?.joinToString(separator = sep)
        richSkillRow.certifications = rsd?.certifications?.map{ it.value }?.joinToString(separator = sep)
        richSkillRow.blsMajors = rsd?.jobCodes?.map { it.majorCode }?.toMutableList()?.filterNotNull()?.distinct()?.joinToString (separator = sep)
        richSkillRow.blsMinors = rsd?.jobCodes?.map { it.minorCode }?.toMutableList()?.filterNotNull()?.distinct()?.joinToString (separator = sep)
        richSkillRow.blsBroads = rsd?.jobCodes?.map { it.broadCode }?.toMutableList()?.filterNotNull()?.distinct()?.joinToString (separator = sep)
        richSkillRow.blsDetaileds = rsd?.jobCodes?.map { it.detailedCode }?.toMutableList()?.filterNotNull()?.distinct()?.joinToString (separator = sep)
        richSkillRow.jobRoles = rsd?.jobCodes?.map { it.jobRoleCode }?.toMutableList()?.filterNotNull()?.distinct()?.joinToString (separator = sep)
        richSkillRow.author = rsd?.author?.value
        richSkillRow.employer = rsd?.employers?.map { it.value }?.joinToString (separator = sep)
        richSkillRow.alignmentTitle = rsd?.alignments?.map { it.value }?.joinToString (separator = sep)
        richSkillRow.alignmentUri = rsd?.alignments?.map { it.uri }?.joinToString (separator = sep)

        return richSkillRow
    }

    private fun jobCode2BlsJobCode(
            bls: JobCode
    ): BlsJobCode {
        val blsJobCode = BlsJobCode()
        if(bls.framework=="bls") {
            if (bls.minor.isNullOrEmpty() && bls.major.isNullOrEmpty()) {
                blsJobCode.socGroup = "Broad"
                blsJobCode.code = bls.code
                blsJobCode.socTitle = bls.broad
                blsJobCode.socDefinition = ""
            } else {
                if (bls.broad.isNullOrEmpty() && bls.major.isNullOrEmpty()) {
                    blsJobCode.socGroup = "Minor"
                    blsJobCode.code = bls.code
                    blsJobCode.socTitle = bls.minor
                    blsJobCode.socDefinition = ""
                } else {
                    if (bls.broad.isNullOrEmpty() && bls.minor.isNullOrEmpty()) {
                        blsJobCode.socGroup = "Major"
                        blsJobCode.code = bls.code
                        blsJobCode.socTitle = bls.major
                        blsJobCode.socDefinition = ""
                    } else {
                        blsJobCode.socGroup = "Detailed"
                        blsJobCode.code = bls.code
                        blsJobCode.socTitle = bls.detailed
                        blsJobCode.socDefinition = bls.description
                    }
                }
            }
        }
        return blsJobCode
    }

    private fun jobCode2OnetJobCode(
            onet: JobCode
    ): OnetJobCode {
        val onetJobCode = OnetJobCode()
        if(onet.framework=="o*net") {
            onetJobCode.code = onet.code
            onetJobCode.title = onet.name
            onetJobCode.description = onet.description
        }
        return onetJobCode
    }

    // **************************
    //       Helper Methods
    // **************************
    private fun createAppConfig(): AppConfig {
        return AppConfig(
            "osmt.wgu.edu",
            "https://osmt.wgu.edu",
            "Default Author Name",
            "https://osmt.wgu.edu/author",
            "https://osmt.wgu.edu/credentialengineerregistry",
            "http://localhost:4200",
            "http://localhost:4200/login/success",
            "user",
            "user@email.com",
            true,
            true,
            false,
            true,
            "https://rsd.openskillsnetwork.org/context-v1.json",
            "http://localhost:4200",
            "Osmt_Admin",
            "Osmt_Curator",
            "Osmt_View",
            "SCOPE_osmt.read"
        )
    }

    private fun parseDateTime(dateTime: String?): LocalDateTime? {
        return if (dateTime == null || dateTime.isEmpty()) {
            null
        } else LocalDateTime.parse(dateTime, formatter)
    }

    private fun populateKeywords(osmtData: OsmtData) {
        osmtData.keywords!!.stream().forEach { kw: edu.wgu.skills.service.mockdata.xml.Keyword ->
            keywords[kw.id] = Keyword(
                kw.id,
                parseDateTime(kw.creationDate)!!,
                parseDateTime(kw.updateDate)!!,
                KeywordTypeEnum.valueOf(kw.keywordTypeEnum!!),
                kw.value,
                kw.uri
            )
        }
    }

    private fun populateCollections(osmtData: OsmtData) {
        osmtData.collections!!.stream().forEach { c: Collection ->
            val skillUUIDs: MutableList<String?> =
                ArrayList()
            listOf(*c.skillsIds!!.split(",").toTypedArray())
                .stream()
                .forEach { sid: String? ->
                    val r =
                        osmtData.richSkillDocs!!.stream()
                            .filter { rsd: edu.wgu.skills.service.mockdata.xml.RichSkillDoc ->
                                sid != null && rsd.id == sid.toLong()
                            }.findAny()
                    if (r.isPresent) {
                        skillUUIDs.add(r.get().uuid)
                    } else {
                        log.info("Unable to find skill id {}", sid)
                    }
                }
            collections[c.id] = CollectionDoc(
                c.id!!,
                c.uuid!!,
                c.name!!,
                c.status?.let { PublishStatus.forApiValue(it) }!!,
                skillUUIDs as List<String>,
                c.skillsCount,
                lookupKeywordValue(c.author?.toLong()),
                parseDateTime(c.archiveDate),
                parseDateTime(c.publishDate),
                c.workspaceOwner!!
            )
        }
    }

    private fun populateJobCodes(osmtData: OsmtData) {
        osmtData.jobCodes!!.stream().forEach { jc: edu.wgu.skills.service.mockdata.xml.JobCode ->
            jobCodes[jc.id] = JobCode(
                jc.id,
                parseDateTime(jc.creationDate)!!,
                jc.major,
                jc.minor,
                jc.broad,
                jc.detailed,
                jc.code!!,
                jc.name,
                jc.description,
                jc.framework,
                jc.url
            )
        }
    }

    private fun populateRichSkillDescriptor(osmtData: OsmtData) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        osmtData.richSkillDocs!!.stream()
            .forEach { rsd: edu.wgu.skills.service.mockdata.xml.RichSkillDoc ->

                val collections: MutableList<edu.wgu.osmt.collection.Collection> =
                    if (rsd.collections == null) ArrayList() else Arrays.stream(
                        rsd.collections!!.split(",").toTypedArray()
                    )
                        .map { collectionDoc2Collection(this.collections[it?.toLong()]) }
                        .collect(Collectors.toList())

                val jobCodes: MutableList<JobCode> =
                    if (rsd.jobCodes == null) ArrayList() else Arrays.stream(
                        rsd.jobCodes!!.split(",").toTypedArray()
                    )
                        .map { id: String? -> this.jobCodes[id?.toLong()]}
                        .collect(Collectors.toList())

                val keywords: MutableList<Keyword> =
                    if (rsd.keywordValues == null) ArrayList() else Arrays.stream(
                        rsd.keywordValues!!.split(",").toTypedArray()
                    )
                        .map { id: String? -> this.keywords[id?.toLong()]}
                        .collect(Collectors.toList())

                richSkillDescriptor[rsd.uuid] = RichSkillDescriptor(
                    rsd.id,
                    now,
                    now,
                    rsd.uuid!!,
                    rsd.name!!,
                    rsd.statement!!,
                    jobCodes,
                    keywords,
                    lookupKeyword(rsd.categoryKeyword),
                    lookupKeyword(rsd.authorKeyword),
                    parseDateTime(rsd.archiveDate),
                    parseDateTime(rsd.publishDate),
                    collections
                )
            }
    }

    private fun collectionDoc2Collection(doc: CollectionDoc?): edu.wgu.osmt.collection.Collection {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        return edu.wgu.osmt.collection.Collection(
            id = doc?.id,
            creationDate = now,
            updateDate = now,
            uuid = doc?.uuid!!,
            name = doc.name,
            author = lookupKeywordByValue(doc.author),
            archiveDate = doc.archiveDate,
            publishDate = doc.publishDate,
            status = doc.publishStatus
        )
    }

    private fun collection2CollectionDoc(c: edu.wgu.osmt.collection.Collection): CollectionDoc {
        return CollectionDoc(
            id = c.id!!,
            uuid = c.uuid,
            name = c.name,
            author = c.author?.value,
            archiveDate = c.archiveDate,
            publishDate = c.publishDate,
            publishStatus = c.publishStatus(),
            skillCount = 0,
            skillIds = null,
            workspaceOwner = c.workspaceOwner
        )
    }

    private fun lookupKeywordValuesByType(
        type: KeywordTypeEnum,
        keywords: List<Long>
    ): List<String> {
        val ret: MutableList<String> = ArrayList()
        keywords.stream()
            .forEach { kwId: Long ->
                val kw = this.keywords[kwId]
                if (kw!!.type == type) {
                    ret.add(kw.value as String)
                }
            }
        return ret
    }

    private fun lookupJobcodes(ids: List<Long>): List<JobCode> {
        val ret: MutableList<JobCode> = ArrayList()
        ids.stream().forEach { id: Long ->
            val jc = jobCodes[id]
            if (jc != null) {
                ret.add(jc)
            }
        }
        return ret
    }

    private fun lookupCollections(ids: List<Long>): List<CollectionDoc> {
        val ret: MutableList<CollectionDoc> = ArrayList()
        ids.stream().forEach { id: Long ->
            val c = collections[id]
            if (c != null) {
                ret.add(c)
            }
        }
        return ret
    }

    private fun lookupKeywordValue(id: Long?): String? {
        val kw = keywords[id]
        if (kw != null) {
            return kw.value
        }

        return null
    }

    private fun lookupKeyword(id: Long?): Keyword? {
        val kw = keywords[id]
        if (kw != null) {
            return kw
        }

        return null
    }

    private fun lookupKeywordByValue(value: String?): Keyword? {
        for (kw in keywords.values) {
            if (kw.value.equals(value)) {
                return kw
            }
        }

        return null
    }

    companion object {
        /**
         * Main for MockData. This is only used for testing/troubleshooting MockData.
         * @param args String[]
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val mockData = MockData()
            println("keywords count: " + mockData.keywords.size)
            println("collections count: " + mockData.collections.size)
            println("jobCodes count: " + mockData.jobCodes.size)
            println("richSkillDescriptors count: " + mockData.richSkillDescriptor.size)
        }
    }
}
