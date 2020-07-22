package edu.wgu.osmt

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
// TODO remove once bootstrapping is finished
class HelloWorldController {

    @GetMapping()
    fun helloWorld(): String {
        return "<html><body>" +
                "<p>Hello, world!</p>" +
                "<p><a href=\"/rich-skill/insert-random\">Insert a random Rich Skill</a></p>" +
                "<p><a href=\"/rich-skill\">View all Rich Skills</a></p>" +
                "</body></html>"
    }
}
