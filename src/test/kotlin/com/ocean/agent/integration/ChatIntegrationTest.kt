package com.ocean.agent.integration

import com.ocean.agent.domain.ChatRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.junit.jupiter.api.Assertions.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.ai.openai.api-key=test-api-key",
    "server.port=0"
])
@Disabled("OpenAI API 키가 필요하므로 실제 환경에서만 실행")
class ChatIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `전체 채팅 플로우 통합 테스트`() {
        // 1. 헬스 체크
        val healthResponse = restTemplate.getForEntity("/api/chat/health", Map::class.java)
        assertEquals(HttpStatus.OK, healthResponse.statusCode)
        assertEquals("OK", healthResponse.body?.get("status"))

        // 2. 채팅 요청
        val chatRequest = ChatRequest(
            message = "안녕하세요! 테스트입니다.",
            conversationId = "integration-test"
        )
        
        val chatResponse = restTemplate.postForEntity(
            "/api/chat",
            chatRequest,
            Map::class.java
        )
        
        assertEquals(HttpStatus.OK, chatResponse.statusCode)
        assertNotNull(chatResponse.body?.get("message"))
        assertEquals("integration-test", chatResponse.body?.get("conversationId"))

        // 3. 대화 기록 조회
        val conversationResponse = restTemplate.getForEntity(
            "/api/chat/conversations/integration-test",
            List::class.java
        )
        
        assertEquals(HttpStatus.OK, conversationResponse.statusCode)
        assertTrue((conversationResponse.body?.size ?: 0) >= 2)

        // 4. 대화 기록 삭제
        restTemplate.delete("/api/chat/conversations/integration-test")
        
        // 5. 삭제 확인
        val deletedConversationResponse = restTemplate.getForEntity(
            "/api/chat/conversations/integration-test",
            List::class.java
        )
        
        assertEquals(HttpStatus.OK, deletedConversationResponse.statusCode)
        assertTrue(deletedConversationResponse.body?.isEmpty() ?: false)
    }

    @Test
    fun `MCP 서비스 통합 테스트`() {
        // 1. MCP 헬스 체크
        val healthResponse = restTemplate.getForEntity("/api/mcp/health", Map::class.java)
        assertEquals(HttpStatus.OK, healthResponse.statusCode)
        assertEquals("OK", healthResponse.body?.get("status"))

        // 2. 서버 목록 조회
        val serversResponse = restTemplate.getForEntity("/api/mcp/servers", List::class.java)
        assertEquals(HttpStatus.OK, serversResponse.statusCode)
        assertTrue((serversResponse.body?.size ?: 0) > 0)

        // 3. 도구 목록 조회
        val toolsResponse = restTemplate.getForEntity("/api/mcp/tools", List::class.java)
        assertEquals(HttpStatus.OK, toolsResponse.statusCode)
        assertTrue((toolsResponse.body?.size ?: 0) > 0)

        // 4. 서버 상태 조회
        val statusResponse = restTemplate.getForEntity(
            "/api/mcp/servers/filesystem-server/status",
            Map::class.java
        )
        assertEquals(HttpStatus.OK, statusResponse.statusCode)
        assertNotNull(statusResponse.body?.get("status"))
    }
}
