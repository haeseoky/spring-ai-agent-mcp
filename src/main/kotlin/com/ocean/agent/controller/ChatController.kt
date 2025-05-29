package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.ChatService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asPublisher
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 채팅 API 컨트롤러
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"]) // 개발용, 운영시에는 특정 도메인으로 제한
class ChatController(
    private val chatService: ChatService
) {
    
    /**
     * 일반 채팅 메시지 전송
     */
    @PostMapping
    suspend fun chat(@RequestBody request: ChatRequest): ChatResponse {
        return chatService.chat(request)
    }
    
    /**
     * 스트리밍 채팅 (Server-Sent Events)
     */
    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatStream(@RequestBody request: ChatRequest): Flux<StreamChatResponse> {
        val flow: Flow<StreamChatResponse> = chatService.chatStream(request)
        return Flux.from(flow.asPublisher())
    }
    
    /**
     * 대화 기록 조회
     */
    @GetMapping("/conversations/{conversationId}")
    fun getConversation(@PathVariable conversationId: String): Mono<List<ChatMessage>> {
        return Mono.just(chatService.getConversation(conversationId))
    }
    
    /**
     * 모든 대화 목록 조회
     */
    @GetMapping("/conversations")
    fun getAllConversations(): Mono<Map<String, List<ChatMessage>>> {
        return Mono.just(chatService.getAllConversations())
    }
    
    /**
     * 대화 기록 삭제
     */
    @DeleteMapping("/conversations/{conversationId}")
    fun clearConversation(@PathVariable conversationId: String): Mono<Map<String, String>> {
        chatService.clearConversation(conversationId)
        return Mono.just(mapOf("message" to "Conversation cleared successfully"))
    }
    
    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    fun health(): Mono<Map<String, String>> {
        return Mono.just(mapOf("status" to "OK", "service" to "ChatService"))
    }
}
