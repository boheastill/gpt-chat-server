package kit6.top.gptchatserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GptChatServerApplication

fun main(args: Array<String>) {
    runApplication<GptChatServerApplication>(*args)
}
