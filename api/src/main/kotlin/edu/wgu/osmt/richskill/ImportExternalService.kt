package edu.wgu.osmt.richskill

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiSkill
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.HttpURLConnection
import java.net.URL

@Service
@Transactional
class ImportExternalService {
    val mapper = jacksonObjectMapper()

    private inline fun <reified T>fetchFromUrl(url: String): T? {
        val httpConnection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        httpConnection.requestMethod = "GET"
        httpConnection.addRequestProperty("Accept", "application/json")

        if (httpConnection.responseCode == 200) {
            try {
                val apiSkill: T = mapper.readValue(httpConnection.inputStream)
                return apiSkill
            } catch (e: JsonParseException) {
                return null
            }
        } else {
            return null
        }
    }

    fun fetchSkillFromUrl(url: String): ApiSkill? {
        return fetchFromUrl<ApiSkill>(url)
    }
    fun fetchCollectionFromUrl(url: String): ApiCollection? {
        return fetchFromUrl<ApiCollection>(url)
    }

}