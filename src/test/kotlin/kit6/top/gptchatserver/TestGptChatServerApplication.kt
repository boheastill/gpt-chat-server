package kit6.top.gptchatserver

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<GptChatServerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
