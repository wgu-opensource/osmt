package edu.wgu.osmt.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class ControllerInterceptor @Autowired internal constructor(currentPath: CurrentPath): HandlerInterceptor {

    private val currentPath: CurrentPath

    init {
        this.currentPath =  currentPath
    }

    override fun preHandle(
            request: HttpServletRequest, response: HttpServletResponse, handler: Any
    ): Boolean {

        println("----------------------")
        println("hitting: ${request.requestURI}")
        currentPath.currentPath = request.requestURI
        return true
    }
}
