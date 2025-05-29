package com.ocean.agent.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 채팅 메시지를 표현하는 도메인 모델
 */
data class ChatMessage(
    val id: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 채팅 요청 모델
 */
data class ChatRequest(
    val message: String,
    val conversationId: String? = null,
    val useTools: Boolean = true
)

/**
 * 채팅 응답 모델
 */
data class ChatResponse(
    val id: String,
    val message: String,
    val conversationId: String,
    val toolsUsed: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 스트리밍 채팅 응답 모델
 */
data class StreamChatResponse(
    val id: String,
    val delta: String,
    val conversationId: String,
    val isComplete: Boolean = false,
    val toolsUsed: List<String> = emptyList()
)
