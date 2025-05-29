package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.ChatService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.util.*

@WebFluxTest(ChatController::class)
class ChatControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var chatService: ChatService

    @Test
    fun `일반 채팅 요청 테스트`() {
        // Given
        val request = ChatRequest(
            message = "안녕하세요",
            conversationId = "test-conversation"
        )
        
        val response = ChatResponse(
            id = UUID.randomUUID().toString(),
            message = "안녕하세요! 무엇을 도와드릴까요?",
            conversationId = "test-conversation",
            toolsUsed = emptyList()
        )
        
        runBlocking {
            `when`(chatService.chat(any())).thenReturn(response)
        }

        // When & Then
        webTestClient.post()
            .uri("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("안녕하세요! 무엇을 도와드릴까요?")
            .jsonPath("$.conversationId").isEqualTo("test-conversation")
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
        
        `when`(chatService.chatStream(any())).thenReturn(streamResponses)

        // When & Then
        webTestClient.post()
            .uri("/api/chat/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
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
        
        `when`(chatService.getConversation(conversationId)).thenReturn(messages)

        // When & Then
        webTestClient.get()
            .uri("/api/chat/conversations/$conversationId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ChatMessage::class.java)
            .hasSize(2)
    }

    @Test
    fun `대화 기록 삭제 테스트`() {
        // Given
        val conversationId = "test-conversation"

        // When & Then
        webTestClient.delete()
            .uri("/api/chat/conversations/$conversationId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Conversation cleared successfully")
            
        verify(chatService).clearConversation(conversationId)
    }

    @Test
    fun `헬스 체크 테스트`() {
        // When & Then
        webTestClient.get()
            .uri("/api/chat/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("OK")
            .jsonPath("$.service").isEqualTo("ChatService")
    }
}
