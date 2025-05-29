package com.ocean.agent.service

import com.ocean.agent.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * AI 채팅 서비스
 * Spring AI를 활용하여 OpenAI와 통신하고 MCP 도구를 연동
 */
@Service
class ChatService(
    private val chatModel: OpenAiChatModel,
    private val mcpService: McpService
) {
    
    private val conversations = ConcurrentHashMap<String, List<ChatMessage>>()
    
    /**
     * 일반 채팅 응답 생성
     */
    suspend fun chat(request: ChatRequest): com.ocean.agent.domain.ChatResponse {
        val conversationId = request.conversationId ?: UUID.randomUUID().toString()
        
        // 사용자 메시지 저장
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = request.message
        )
        addMessageToConversation(conversationId, userMessage)
        
        // AI 응답 생성
        val response = ChatClient.create(chatModel)
            .prompt()
            .user(request.message)
            .call()
            .content()
        
        val responseContent = response ?: "응답을 생성할 수 없습니다."
        
        // 어시스턴트 메시지 저장
        val assistantMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "assistant",
            content = responseContent
        )
        addMessageToConversation(conversationId, assistantMessage)
        
        return com.ocean.agent.domain.ChatResponse(
            id = assistantMessage.id,
            message = responseContent,
            conversationId = conversationId,
            toolsUsed = emptyList()
        )
    }
    
    /**
     * 스트리밍 채팅 응답 생성
     */
    fun chatStream(request: ChatRequest): Flow<StreamChatResponse> = flow {
        val conversationId = request.conversationId ?: UUID.randomUUID().toString()
        val responseId = UUID.randomUUID().toString()
        
        // 사용자 메시지 저장
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = request.message
        )
        addMessageToConversation(conversationId, userMessage)
        
        val responseBuilder = StringBuilder()
        
        try {
            // 스트리밍 응답 생성
            val response = ChatClient.create(chatModel)
                .prompt()
                .user(request.message)
                .call()
                .content()
            
            // 간단한 스트리밍 시뮬레이션
            val words = response?.split(" ") ?: emptyList()
            words.forEach { word ->
                val delta = "$word "
                responseBuilder.append(delta)
                
                emit(StreamChatResponse(
                    id = responseId,
                    delta = delta,
                    conversationId = conversationId,
                    isComplete = false,
                    toolsUsed = emptyList()
                ))
            }
            
            // 완료 신호 발송
            emit(StreamChatResponse(
                id = responseId,
                delta = "",
                conversationId = conversationId,
                isComplete = true,
                toolsUsed = emptyList()
            ))
            
            // 어시스턴트 메시지 저장
            val assistantMessage = ChatMessage(
                id = responseId,
                role = "assistant",
                content = responseBuilder.toString()
            )
            addMessageToConversation(conversationId, assistantMessage)
            
        } catch (e: Exception) {
            emit(StreamChatResponse(
                id = responseId,
                delta = "Error: ${e.message}",
                conversationId = conversationId,
                isComplete = true
            ))
        }
    }
    
    /**
     * 대화 기록 조회
     */
    fun getConversation(conversationId: String): List<ChatMessage> {
        return conversations[conversationId] ?: emptyList()
    }
    
    /**
     * 대화 기록 삭제
     */
    fun clearConversation(conversationId: String) {
        conversations.remove(conversationId)
    }
    
    /**
     * 모든 대화 목록 조회
     */
    fun getAllConversations(): Map<String, List<ChatMessage>> {
        return conversations.toMap()
    }
    
    /**
     * 대화에 메시지 추가
     */
    private fun addMessageToConversation(conversationId: String, message: ChatMessage) {
        val currentMessages = conversations[conversationId] ?: emptyList()
        conversations[conversationId] = currentMessages + message
    }
    
    /**
     * MCP 도구 실행을 위한 함수 정의
     */
    fun executeReadFile(path: String): String {
        return try {
            val response = kotlinx.coroutines.runBlocking {
                mcpService.executeTool("read_file", mapOf("path" to path))
            }
            if (response.success) {
                response.result as? String ?: "파일을 읽었지만 내용이 비어있습니다."
            } else {
                "파일 읽기 실패: ${response.error}"
            }
        } catch (e: Exception) {
            "파일 읽기 중 오류 발생: ${e.message}"
        }
    }
    
    fun executeWriteFile(path: String, content: String): String {
        return try {
            val response = kotlinx.coroutines.runBlocking {
                mcpService.executeTool("write_file", mapOf("path" to path, "content" to content))
            }
            if (response.success) {
                "파일이 성공적으로 저장되었습니다."
            } else {
                "파일 저장 실패: ${response.error}"
            }
        } catch (e: Exception) {
            "파일 저장 중 오류 발생: ${e.message}"
        }
    }
    
    fun executeWebSearch(query: String, limit: Int = 5): String {
        return try {
            val response = kotlinx.coroutines.runBlocking {
                mcpService.executeTool("search_web", mapOf("query" to query, "limit" to limit))
            }
            if (response.success) {
                val results = response.result as? List<Map<String, Any>>
                results?.joinToString("\n\n") { result ->
                    "제목: ${result["title"]}\nURL: ${result["url"]}\n요약: ${result["snippet"]}"
                } ?: "검색 결과가 없습니다."
            } else {
                "웹 검색 실패: ${response.error}"
            }
        } catch (e: Exception) {
            "웹 검색 중 오류 발생: ${e.message}"
        }
    }
}
