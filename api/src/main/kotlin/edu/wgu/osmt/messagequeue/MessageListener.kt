package edu.wgu.osmt.messagequeue

import com.github.sonus21.rqueue.annotation.RqueueListener
import org.springframework.stereotype.Component

@Component
class MessageListener {


    // TODO do something more useful
    @RqueueListener(value = ["job"])
    fun handleJob(s: String) {
        println(s)
    }
}
