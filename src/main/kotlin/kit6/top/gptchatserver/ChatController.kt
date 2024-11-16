package kit6.top.gptchatserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/gpt-chat-server/chat")
class ChatController @Autowired constructor(
    private val multiChatManager: MultiChatManager
) {
    // 定义 tokenMap，键为 token，值为字数上限
    private val tokenMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap(
        mapOf(
            "hello" to 10000,
            "hello1" to 10000,
            "hello2" to 10000,
        )
    )

    @PostMapping("/{sessionId}")
    fun sendMessage(@PathVariable sessionId: String, @RequestBody input: Map<String, Any>): String {
        return multiChatManager.sendMessage(sessionId, input)
    }

    @GetMapping("/test")
    fun sendMessage(@RequestParam input: String, @RequestParam token: String): String {
        // 获取字数上限
        val charLimit = tokenMap[token] ?: throw Exception("Invalid token")

        // 校验输入长度是否超出字数上限
        if (input.length > charLimit) {
            throw Exception("Input exceeds the character limit of $charLimit for token $token")
        }

        // 减少对应 token 的额度
        synchronized(this) {
            val remainingLimit = tokenMap[token] ?: throw Exception("Invalid token")
            if (input.length > remainingLimit) {
                throw Exception("Not enough remaining characters for token $token. Current limit: $remainingLimit")
            }
            // 扣除使用的字数
            tokenMap[token] = remainingLimit - input.length
        }

        // 发送消息并返回结果
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
