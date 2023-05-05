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
        if (authorization == null) {
            val status: Array<String> = request.getParameterValues("status")
            val result = status.map { PublishStatus.forApiValue(it) }.filter { s -> s != PublishStatus.Deleted && s != PublishStatus.Draft}
            request.setAttribute("message", "hello")
            request.setAttribute("status", result.toTypedArray())
            System.out.println(request.getParameter("message"))
            // request.setAttribute("status", request.getParameterValues("status").filter { it != "draft" && it != "delete" })
            // request.getParameterValues("status").filter { it != "draft" && it != "delete" }
        }
        return true
    }


}