package kit6.top.gptchatserver

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import org.junit.jupiter.api.Assertions.assertEquals

@SpringBootTest
class MultiChatManagerTest {

    private lateinit var multiChatManager: MultiChatManager

    @Autowired
    private lateinit var builder: ChatClient.Builder

    @BeforeEach
    fun setUp() {
        multiChatManager = MultiChatManager(builder)
    }

    @Test
    fun testSendMessage() {
        val sessionId = "session1"
        val input = mapOf("userMessage" to "天王盖地虎")

        val assistantResponse = multiChatManager.sendMessage(sessionId, input)
        println("gptsay: $assistantResponse")
        assertEquals("白菜俩块五", assistantResponse) // 根据实际的 GPT 响应调整期望值
    }


}
