package kit6.top.gptchatserver

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Component

@Component
class MultiChatManager(builder: ChatClient.Builder) {
    private val logger: Logger = LoggerFactory.getLogger(MultiChatManager::class.java)
    private val chatClient: ChatClient = builder.build()
    private val sessions: MutableMap<String, ChatSession> = HashMap()
    private val SUMMARY_PREFIX = "[SUMMARY:]"

    fun sendMessage(sessionId: String, input: Map<String, Any>): String {
        logger.info("Sending structured message for session {}: {}", sessionId, input)

        val session = sessions.computeIfAbsent(sessionId) {
            logger.info("Creating new session for ID: {}", sessionId)
            ChatSession()
        }

        parseInput(input, session)

        val context = session.buildContext()
        val prompt = "$context\n请在回复末尾添加摘要，格式为：$SUMMARY_PREFIX 你的摘要内容"

        val response = chatClient.prompt(prompt).call().chatResponse()
        val assistantResponseContent = response.result.output.content

        val summary = parseSummary(assistantResponseContent)

        val assistantMessage = AssistantMessage(assistantResponseContent)
        session.addAssistantMessage(assistantMessage)
        session.addSummary(summary)

        logger.info("Assistant response for session {}: {}", sessionId, assistantResponseContent)

        return assistantMessage.content
    }
    fun sendMessage(inputText: String): String {
        // 记录发送消息日志
        logger.info("Sending message: {}", inputText)

        // 构造请求文本，包含摘要请求
        val prompt = "我在学习术，请协助我，协助前尝试总结主题，关注正文，适当扩展，最后简短建议。帮我深入原理、批判性回答、简练不客套。最终返回我时，尽量用中文。以下正文：\n$inputText"
        // 调用聊天客户端并获取响应
        val response = chatClient.prompt(prompt).call().chatResponse()
        val assistantResponseContent = response.result.output.content

        // 记录响应日志
        logger.info("Assistant response: {}", assistantResponseContent)

        return assistantResponseContent
    }

    private fun parseInput(input: Map<String, Any>, session: ChatSession) {
        val userMessage = input["userMessage"] as? String
        userMessage?.let {
            val message = UserMessage(it)
            session.addUserMessage(message)
        }
    }

    private fun parseSummary(responseContent: String): String {
        val summaryStart = responseContent.indexOf(SUMMARY_PREFIX)
        return if (summaryStart != -1) {
            responseContent.substring(summaryStart + SUMMARY_PREFIX.length).trim()
        } else {
            ""
        }
    }

    fun getSession(sessionId: String): ChatSession {
        logger.info("Retrieving session for ID: {}", sessionId)
        return sessions[sessionId] ?: throw NoSuchElementException("Session not found")
    }

    fun removeSession(sessionId: String) {
        logger.info("Removing session for ID: {}", sessionId)
        sessions.remove(sessionId)
    }

    class ChatSession {
        private val userMessages: MutableList<UserMessage> = ArrayList()
        private val assistantMessages: MutableList<AssistantMessage> = ArrayList()
        private val summaries: MutableList<String> = ArrayList()

        fun addUserMessage(message: UserMessage) {
            userMessages.add(message)
        }

        fun addAssistantMessage(message: AssistantMessage) {
            assistantMessages.add(message)
        }

        fun addSummary(summary: String) {
            summaries.add(summary)
        }

        fun buildContext(): String {
            val contextBuilder = StringBuilder()

            summaries.forEach { summary ->
                contextBuilder.append(summary).append("\n")
            }
            userMessages.forEach { userMessage ->
                contextBuilder.append(userMessage.content).append("\n")
            }
            assistantMessages.forEach { assistantMessage ->
                contextBuilder.append(assistantMessage.content).append("\n")
            }

            return contextBuilder.toString()
        }
    }
}
