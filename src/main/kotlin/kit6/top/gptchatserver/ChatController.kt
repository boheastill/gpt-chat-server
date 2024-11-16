package kit6.top.gptchatserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@RestController
@RequestMapping("/gpt-chat-server/chat")
class ChatController @Autowired constructor(
    private val multiChatManager: MultiChatManager
) {
    // 定义 tokenMap，键为 token，值为剩余字数
    private val tokenMap: ConcurrentHashMap<String, AtomicInteger> = ConcurrentHashMap(
        mapOf(
            "hello" to AtomicInteger(10000),
            "hello1" to AtomicInteger(10000),
            "hello2" to AtomicInteger(10000),
            "hello3" to AtomicInteger(10000),
            "hello4" to AtomicInteger(10000),
            "hello5" to AtomicInteger(10000),
            "hello6" to AtomicInteger(10000),
            "hello7" to AtomicInteger(10000),
            "hello8" to AtomicInteger(10000),
            "hello9" to AtomicInteger(10000),
        )
    )

    @PostMapping("/{sessionId}")
    fun sendMessage(@PathVariable sessionId: String, @RequestBody input: Map<String, Any>): String {
        return multiChatManager.sendMessage(sessionId, input)
    }

    @GetMapping("/test")
    fun sendMessage(@RequestParam input: String, @RequestParam token: String): String {
        // 检查 token 是否有效
        val remainingLimit = tokenMap[token]?.get() ?: throw TokenInvalidException("Invalid token: $token")

        // 获取输入的实际 token 数量
        val inputTokenCount = calculateTokens(input)

        // 检查剩余额度
        if (inputTokenCount > remainingLimit) {
            throw TokenLimitExceededException("Input exceeds the remaining token limit of $remainingLimit for token $token")
        }

        // 减少额度
        tokenMap[token]?.getAndUpdate { it - inputTokenCount }

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

    // 每天凌晨 0 点重置 tokenMap
//    @Scheduled(cron = "0 0 0 * * ?")
//    fun resetTokenLimits() {
//        tokenMap.forEach { (token, _) -> tokenMap[token]?.set(30000) }
//    }

    // 计算输入的实际 token 数量（伪代码，根据实际 tiktoken REST 服务实现）
    private fun calculateTokens(input: String): Int {
        // 调用外部服务计算 token 数量
        // 示例伪代码，需替换为实际实现
        return input.length
    }
}

// 自定义异常类
class TokenInvalidException(message: String) : RuntimeException(message)
class TokenLimitExceededException(message: String) : RuntimeException(message)
