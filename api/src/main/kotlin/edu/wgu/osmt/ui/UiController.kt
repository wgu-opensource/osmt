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

    /*
     * Serve the location of the "default" whitelabel configuration.  Configure an environment's config location in
     * ui/src/environment/ to override using this location
     */
    @RequestMapping("assets/config.json")
    fun defaultWhiteLabelConfig(): String {
        return javaClass.getResource("/ui/assets/config.json")?.readText(Charsets.UTF_8) ?: "No whitelabel config found"
    }

}
