package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.McpService
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

@WebFluxTest(McpController::class)
class McpControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var mcpService: McpService

    @Test
    fun `MCP 서버 목록 조회 테스트`() {
        // Given
        val servers = listOf(
            McpServer(
                id = "server-1",
                name = "파일 시스템 서버",
                description = "파일 읽기/쓰기 기능 제공",
                command = "npx",
                args = listOf("-y", "@modelcontextprotocol/server-filesystem"),
                enabled = true
            ),
            McpServer(
                id = "server-2",
                name = "Brave 검색 서버",
                description = "웹 검색 기능 제공",
                command = "npx",
                args = listOf("-y", "@modelcontextprotocol/server-brave-search"),
                enabled = true
            )
        )
        
        `when`(mcpService.getAllServers()).thenReturn(servers)

        // When & Then
        webTestClient.get()
            .uri("/api/mcp/servers")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(McpServer::class.java)
            .hasSize(2)
    }

    @Test
    fun `MCP 도구 목록 조회 테스트`() {
        // Given
        val tools = listOf(
            McpTool(
                name = "read_file",
                description = "파일 읽기",
                inputSchema = McpToolSchema(
                    type = "object",
                    properties = mapOf(
                        "path" to McpPropertySchema(
                            type = "string",
                            description = "파일 경로"
                        )
                    ),
                    required = listOf("path")
                ),
                serverId = "server-1"
            ),
            McpTool(
                name = "search_web",
                description = "웹 검색",
                inputSchema = McpToolSchema(
                    type = "object",
                    properties = mapOf(
                        "query" to McpPropertySchema(
                            type = "string",
                            description = "검색어"
                        )
                    ),
                    required = listOf("query")
                ),
                serverId = "server-2"
            )
        )
        
        `when`(mcpService.getAllTools()).thenReturn(tools)

        // When & Then
        webTestClient.get()
            .uri("/api/mcp/tools")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(McpTool::class.java)
            .hasSize(2)
    }

    @Test
    fun `MCP 도구 실행 테스트`() {
        // Given
        val request = McpToolRequest(
            toolName = "read_file",
            arguments = mapOf("path" to "/test/file.txt")
        )
        
        val response = McpToolResponse(
            success = true,
            result = "파일 내용입니다."
        )
        
        runBlocking {
            `when`(mcpService.executeTool(anyString(), any())).thenReturn(response)
        }

        // When & Then
        webTestClient.post()
            .uri("/api/mcp/tools/execute")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.result").isEqualTo("파일 내용입니다.")
    }

    @Test
    fun `MCP 서버 상태 조회 테스트`() {
        // Given
        val server = McpServer(
            id = "server-1",
            name = "파일 시스템 서버",
            description = "파일 읽기/쓰기 기능 제공",
            command = "npx",
            args = listOf("-y", "@modelcontextprotocol/server-filesystem"),
            enabled = true,
            tools = listOf()
        )
        
        `when`(mcpService.getServer("server-1")).thenReturn(server)

        // When & Then
        webTestClient.get()
            .uri("/api/mcp/servers/server-1/status")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("server-1")
            .jsonPath("$.name").isEqualTo("파일 시스템 서버")
            .jsonPath("$.enabled").isEqualTo(true)
    }

    @Test
    fun `헬스 체크 테스트`() {
        // When & Then
        webTestClient.get()
            .uri("/api/mcp/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("OK")
            .jsonPath("$.service").isEqualTo("McpService")
    }
}
