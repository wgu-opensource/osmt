package edu.wgu.osmt.interceptor

import edu.wgu.osmt.db.PublishStatus
import kotlinx.coroutines.newSingleThreadContext
import org.elasticsearch.common.collect.HppcMaps
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class SearchControllerInterceptor: HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest, response: HttpServletResponse, handler: Any
    ): Boolean {
        val authorization: String? = request.getHeader("Authorization")
        var status: Array<String>? = request.getParameterValues("status")
        if (authorization == null) {
            if (status == null || status.isEmpty()) {
                status = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET.split(",").toTypedArray()
            }
            val result = status?.map { PublishStatus.forApiValue(it) }?.filter { s -> s != PublishStatus.Deleted && s != PublishStatus.Draft}
            request.setAttribute("status", result)
        } else {
            if (status == null || status.isEmpty()) {
                status = PublishStatus.DEFAULT_API_PUBLISH_STATUS_SET.split(",").toTypedArray()
            }
            val result = status?.map { PublishStatus.forApiValue(it) }
            request.setAttribute("status", result)
        }
        return true
    }


}