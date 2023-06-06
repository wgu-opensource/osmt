package edu.wgu.osmt.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class ControllerInterceptor @Autowired internal constructor(requestInfo: RequestInfo): HandlerInterceptor {

    private val requestInfo: RequestInfo

    init {
        this.requestInfo =  requestInfo
    }

    override fun preHandle(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any
    ): Boolean {

        this.requestInfo.requestPath = request.requestURI
        return true
    }
}
