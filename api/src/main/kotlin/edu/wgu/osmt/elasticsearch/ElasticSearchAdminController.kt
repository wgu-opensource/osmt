package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.security.OAuthHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ForkJoinPool

@Controller
@Transactional
class ElasticSearchAdminController @Autowired constructor(
    val appConfig: AppConfig,
    val oAuthHelper: OAuthHelper,
    val esReindexer: ElasticSearchReindexer
) {

    @RequestMapping(path = [RoutePaths.Latest.ES_ADMIN_DELETE_INDICES, RoutePaths.Unversioned.ES_ADMIN_DELETE_INDICES, RoutePaths.OldSupported.ES_ADMIN_DELETE_INDICES])
    @PostMapping
    fun deleteElasticSearchIndices(): ResponseEntity<String> {

        if (!oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        ForkJoinPool.commonPool().submit(esReindexer::deleteAllIndices)
        return ResponseEntity(
            "Deleting ElasticSearch indices in the background. Please refer to the logs.",
            HttpStatus.ACCEPTED
        )
    }

    @RequestMapping(path = [RoutePaths.Latest.ES_ADMIN_REINDEX, RoutePaths.Unversioned.ES_ADMIN_REINDEX, RoutePaths.OldSupported.ES_ADMIN_REINDEX])
    @PostMapping
    fun reindexElasticSearch(): ResponseEntity<String> {

        if (!oAuthHelper.hasRole(appConfig.roleAdmin)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        ForkJoinPool.commonPool().submit(esReindexer::reindexAll)
        return ResponseEntity(
            "Reindexing ElasticSearch in the background. Please refer to the logs.",
            HttpStatus.ACCEPTED
        )
    }
}
