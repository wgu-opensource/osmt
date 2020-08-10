package edu.wgu.osmt

import com.github.sonus21.rqueue.core.RqueueMessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("")
// TODO remove once bootstrapping is finished
class HelloWorldController {

    @Autowired
    lateinit var rqueueMessageSender: RqueueMessageSender

    @GetMapping()
    fun helloWorld(@AuthenticationPrincipal user: OAuth2User?): String {
        return "<html><body>" +
                "<p>Hello, world!</p>" +

                "<p>${user?.toString() ?: "No user, maybe you should login? <a href=\"/login\">login</a>"}</p>" +
                "<p>PROTECTED - <a href=\"/skills/insert-random\">Insert a random Rich Skill</a></p>" +
                "<p>PUBLIC - <a href=\"/skills\">View all Rich Skills</a></p>" +
                "${user?.let { "<a href=\"/logout\">logout</a> " } ?: ""} " +
                "</body></html>"
    }

    @GetMapping("/enqueue")
    fun enqueueBackgroundTask(@RequestParam message: String): String {
        rqueueMessageSender.enqueueAt("job", message, Instant.now().plusSeconds(10))
        return "<html>" +
                "<body><p>Message enqueued!</p></body>" +
                "</html>"
    }

}
