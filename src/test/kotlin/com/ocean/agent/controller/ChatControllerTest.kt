package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.ChatService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class ChatControllerTest {

    @Mock
    private lateinit var chatService: ChatService

    @InjectMocks
    private lateinit var chatController: ChatController

    @Test
    fun `일반 채팅 요청 테스트`() = runBlocking {
        // Given
        val request = ChatRequest(
            message = "안녕하세요",
            conversationId = "test-conversation"
        )
        
        val response = ChatResponse(
            id = "test-id",
            message = "안녕하세요! 무엇을 도와드릴까요?",
            conversationId = "test-conversation",
            toolsUsed = emptyList()
        )
        
        whenever(chatService.chat(any<ChatRequest>())).thenReturn(response)

        // When
        val result = chatController.chat(request)

        // Then
        assert(result.message == "안녕하세요! 무엇을 도와드릴까요?")
        assert(result.conversationId == "test-conversation")
        verify(chatService).chat(request)
    }

    @Test
    fun `스트리밍 채팅 요청 테스트`() {
        // Given
        val request = ChatRequest(
            message = "스트리밍 테스트",
            conversationId = "stream-test"
        )
        
        val streamResponses = flowOf(
            StreamChatResponse(
                id = "response-1",
                delta = "안녕",
                conversationId = "stream-test",
                isComplete = false
            ),
            StreamChatResponse(
                id = "response-1",
                delta = "하세요!",
                conversationId = "stream-test",
                isComplete = true
            )
        )
        
        whenever(chatService.chatStream(any<ChatRequest>())).thenReturn(streamResponses)

        // When
        val result = chatController.chatStream(request)

        // Then
        assert(result != null)
        verify(chatService).chatStream(request)
    }

    @Test
    fun `대화 기록 조회 테스트`() {
        // Given
        val conversationId = "test-conversation"
        val messages = listOf(
            ChatMessage(
                id = "msg-1",
                role = "user",
                content = "안녕하세요"
            ),
            ChatMessage(
                id = "msg-2",
                role = "assistant",
                content = "안녕하세요! 무엇을 도와드릴까요?"
            )
        )
        
        whenever(chatService.getConversation(conversationId)).thenReturn(messages)

        // When
        val result = chatController.getConversation(conversationId).block()

        // Then
        assert(result != null)
        assert(result!!.size == 2)
        verify(chatService).getConversation(conversationId)
    }

    @Test
    fun `대화 기록 삭제 테스트`() {
        // Given
        val conversationId = "test-conversation"

        // When
        val result = chatController.clearConversation(conversationId).block()

        // Then
        assert(result != null)
        assert(result!!["message"] == "Conversation cleared successfully")
        verify(chatService).clearConversation(conversationId)
    }

    @Test
    fun `헬스 체크 테스트`() {
        // When
        val result = chatController.health().block()

        // Then
        assert(result != null)
        assert(result!!["status"] == "OK")
        assert(result["service"] == "ChatService")
    }

    @Test
    fun `모든 대화 목록 조회 테스트`() {
        // Given
        val conversations = mapOf(
            "conversation-1" to listOf(
                ChatMessage(id = "msg-1", role = "user", content = "안녕하세요")
            )
        )
        
        whenever(chatService.getAllConversations()).thenReturn(conversations)

        // When
        val result = chatController.getAllConversations().block()

        // Then
        assert(result != null)
        assert(result!!.isNotEmpty())
        verify(chatService).getAllConversations()
    }
}
