package edu.wgu.osmt.ui

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class UiController {

    @RequestMapping()
    fun index(): String {
        return javaClass.getResource("/ui/index.html")?.readText(Charsets.UTF_8) ?: "UI not configured"
    }

}
