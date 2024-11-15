package kit6.top.gptchatserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController @Autowired constructor(
    private val multiChatManager: MultiChatManager
) {

    @PostMapping("/{sessionId}")
    fun sendMessage(@PathVariable sessionId: String, @RequestBody input: Map<String, Any>): String {
        return multiChatManager.sendMessage(sessionId, input)
    }

    @GetMapping("/test")
    fun sendMessage(@RequestParam input: String, @RequestParam token: String): String {
        if (token != "hello") {
            throw Exception("Wrong token")
        }
        return multiChatManager.sendMessage(input)
    }


    @GetMapping("/{sessionId}")
    fun getSession(@PathVariable sessionId: String): MultiChatManager.ChatSession {
        return multiChatManager.getSession(sessionId)
    }

    @DeleteMapping("/{sessionId}")
    fun removeSession(@PathVariable sessionId: String) {
        multiChatManager.removeSession(sessionId)
    }
}
