package edu.wgu.osmt.jobcode

import edu.wgu.osmt.auditlog.AuditLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Service
@RestController
@RequestMapping("/jobcode")
@Transactional
class JobCodeApi @Autowired constructor(
    val jobCodeRepository: JobCodeRepository,
    val auditLogRepository: AuditLogRepository
) {

    @GetMapping()
    fun findAll() = jobCodeRepository.findAll()

}
