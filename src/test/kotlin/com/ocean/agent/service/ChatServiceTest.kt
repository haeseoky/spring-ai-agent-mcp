package com.ocean.agent.service

import com.ocean.agent.domain.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel

class ChatServiceTest {

    private lateinit var chatModel: OpenAiChatModel
    private lateinit var mcpService: McpService
    private lateinit var chatService: ChatService

    @BeforeEach
    fun setUp() {
        chatModel = mock(OpenAiChatModel::class.java)
        mcpService = mock(McpService::class.java)
        chatService = ChatService(chatModel, mcpService)
    }

    @Test
    fun `일반 채팅 응답 생성 테스트`() = runBlocking {
        // Given
        val request = ChatRequest(
            message = "안녕하세요",
            conversationId = "test-conversation"
        )

        // ChatClient 모킹이 복잡하므로 실제 테스트에서는 통합 테스트로 진행
        // 여기서는 대화 저장 기능만 테스트
        val response = chatService.chat(request)

        // Then
        assertNotNull(response)
        assertEquals("test-conversation", response.conversationId)
        
        // 대화 기록 확인
        val conversation = chatService.getConversation("test-conversation")
        assertEquals(2, conversation.size) // 사용자 메시지 + AI 응답
    }

    @Test
    fun `스트리밍 채팅 응답 생성 테스트`() = runBlocking {
        // Given
        val request = ChatRequest(
            message = "스트리밍 테스트",
            conversationId = null
        )

        // When
        val responses = chatService.chatStream(request).toList()

        // Then
        assertTrue(responses.isNotEmpty())
        assertTrue(responses.last().isComplete)
        assertNotNull(responses.first().conversationId)
    }

    @Test
    fun `대화 기록 관리 테스트`() {
        // Given
        val conversationId = "test-conv"
        
        // 초기 상태 확인
        assertTrue(chatService.getConversation(conversationId).isEmpty())
        
        // 모든 대화 목록 확인
        val allConversations = chatService.getAllConversations()
        assertFalse(allConversations.containsKey(conversationId))
    }

    @Test
    fun `대화 기록 삭제 테스트`() = runBlocking {
        // Given
        val conversationId = "delete-test"
        val request = ChatRequest(
            message = "삭제 테스트",
            conversationId = conversationId
        )
        
        // 대화 생성
        chatService.chat(request)
        
        // 대화가 저장되었는지 확인
        assertTrue(chatService.getConversation(conversationId).isNotEmpty())
        
        // When
        chatService.clearConversation(conversationId)
        
        // Then
        assertTrue(chatService.getConversation(conversationId).isEmpty())
    }

    @Test
    fun `MCP 도구 실행 - 파일 읽기 테스트`() = runBlocking {
        // Given
        val toolResponse = McpToolResponse(
            success = true,
            result = "파일 내용"
        )
        
        `when`(mcpService.executeTool(eq("read_file"), any())).thenReturn(toolResponse)
        
        // When
        val result = chatService.executeReadFile("/test/file.txt")
        
        // Then
        assertEquals("파일 내용", result)
        verify(mcpService).executeTool(eq("read_file"), any())
    }

    @Test
    fun `MCP 도구 실행 - 파일 쓰기 테스트`() = runBlocking {
        // Given
        val toolResponse = McpToolResponse(
            success = true
        )
        
        `when`(mcpService.executeTool(eq("write_file"), any())).thenReturn(toolResponse)
        
        // When
        val result = chatService.executeWriteFile("/test/file.txt", "새로운 내용")
        
        // Then
        assertEquals("파일이 성공적으로 저장되었습니다.", result)
        verify(mcpService).executeTool(eq("write_file"), any())
    }

    @Test
    fun `MCP 도구 실행 - 웹 검색 테스트`() = runBlocking {
        // Given
        val searchResults = listOf(
            mapOf(
                "title" to "검색 결과 1",
                "url" to "https://example.com/1",
                "snippet" to "결과 요약 1"
            ),
            mapOf(
                "title" to "검색 결과 2",
                "url" to "https://example.com/2",
                "snippet" to "결과 요약 2"
            )
        )
        
        val toolResponse = McpToolResponse(
            success = true,
            result = searchResults
        )
        
        `when`(mcpService.executeTool(eq("search_web"), any())).thenReturn(toolResponse)
        
        // When
        val result = chatService.executeWebSearch("테스트 검색어")
        
        // Then
        assertTrue(result.contains("검색 결과 1"))
        assertTrue(result.contains("https://example.com/1"))
        verify(mcpService).executeTool(eq("search_web"), any())
    }

    @Test
    fun `MCP 도구 실행 실패 처리 테스트`() = runBlocking {
        // Given
        val toolResponse = McpToolResponse(
            success = false,
            error = "도구 실행 실패"
        )
        
        `when`(mcpService.executeTool(any(), any())).thenReturn(toolResponse)
        
        // When
        val result = chatService.executeReadFile("/test/file.txt")
        
        // Then
        assertTrue(result.contains("파일 읽기 실패"))
        assertTrue(result.contains("도구 실행 실패"))
    }
}
