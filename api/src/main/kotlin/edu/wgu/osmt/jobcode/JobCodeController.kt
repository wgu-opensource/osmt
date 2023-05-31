package edu.wgu.osmt.jobcode;

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.api.model.ApiJobCode
import edu.wgu.osmt.api.model.JobCodeUpdate
import edu.wgu.osmt.elasticsearch.OffsetPageable
import edu.wgu.osmt.task.TaskResult
import edu.wgu.osmt.task.TaskStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException

@Controller
@Transactional
class JobCodeController @Autowired constructor(
    val jobCodeEsRepo: JobCodeEsRepo,
    val jobCodeRepository: JobCodeRepository
) {

    @GetMapping(RoutePaths.JOB_CODE_LIST, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun allPaginated(
        @RequestParam(required = true) size: Int,
        @RequestParam(required = true) from: Int,
        @RequestParam(required = false) sort: String?
    ): HttpEntity<List<ApiJobCode>> {
        val searchResults = jobCodeEsRepo.typeAheadSearch("", OffsetPageable(from, size, null))
        return ResponseEntity.status(200).body(searchResults.map { ApiJobCode.fromJobCode(it.content) }.toList())
    }

    @GetMapping(RoutePaths.JOB_CODE_DETAIL, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun byId(
        @PathVariable id: Long,
    ): HttpEntity<ApiJobCode> {
        val jobCode = jobCodeRepository.findById(id)
        if (jobCode != null) {
            return ResponseEntity.status(200).body(ApiJobCode.fromJobCode(jobCode.toModel()))
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping(RoutePaths.JOB_CODE_CREATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun createJobCode(
        @RequestBody jobCodes: List<JobCodeUpdate>
    ): HttpEntity<List<ApiJobCode>> {
        val newJobCodes = jobCodeRepository.createFromApi(jobCodes)
        return ResponseEntity.status(200).body(newJobCodes.map { ApiJobCode.fromJobCode(it.toModel()) }.toList())
    }

    @PostMapping(RoutePaths.JOB_CODE_UPDATE, produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun updateJobCode(
        @PathVariable id: Int,
        @RequestBody jobCodeUpdate: JobCodeUpdate
    ): HttpEntity<ApiJobCode> {
        return ResponseEntity.status(200).body(ApiJobCode(code = "1", targetNode = "target", targetNodeName = "targetNodeName", frameworkName = "frameworkName", parents = listOf()))
    }

    @DeleteMapping(RoutePaths.JOB_CODE_REMOVE)
    @PreAuthorize("hasAuthority(@appConfig.roleAdmin)")
    fun deleteJobCode(
        @PathVariable id: Int,
    ): HttpEntity<TaskResult> {
        return ResponseEntity.status(200).body(TaskResult(uuid = "uuid", contentType = "application/json", status = TaskStatus.Processing, apiResultPath = "path"))
    }

}