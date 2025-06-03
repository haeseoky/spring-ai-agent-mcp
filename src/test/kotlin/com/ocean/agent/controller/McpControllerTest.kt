package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.McpService
import org.junit.jupiter.api.Test
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class McpControllerTest {

    @Mock
    private lateinit var mcpService: McpService

    @InjectMocks
    private lateinit var mcpController: McpController

    @Test
    fun `헬스 체크 테스트`() {
        // Given
        val enabledServers = listOf(
            McpServer(
                id = "server-1",
                name = "테스트 서버",
                description = "테스트용 서버",
                command = "node",
                enabled = true
            )
        )
        val tools = listOf(
            McpTool(
                name = "test_tool",
                description = "테스트 도구",
                inputSchema = McpToolSchema(),
                serverId = "server-1"
            )
        )
        
        whenever(mcpService.getEnabledServers()).thenReturn(enabledServers)
        whenever(mcpService.getAllTools()).thenReturn(tools)

        // When
        val result = mcpController.health().block()

        // Then
        assert(result != null)
        assert(result!!["status"] == "OK")
        assert(result["service"] == "McpService")
        assert(result["enabledServers"] == 1)
        assert(result["totalTools"] == 1)
    }

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
        
        whenever(mcpService.getAllServers()).thenReturn(servers)

        // When
        val result = mcpController.getAllServers().block()

        // Then
        assert(result != null)
        assert(result!!.size == 2)
        verify(mcpService).getAllServers()
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
        
        whenever(mcpService.getAllTools()).thenReturn(tools)

        // When
        val result = mcpController.getAllTools().block()

        // Then
        assert(result != null)
        assert(result!!.size == 2)
        verify(mcpService).getAllTools()
    }

    @Test
    fun `MCP 도구 실행 테스트`() = runBlocking {
        // Given
        val request = McpToolRequest(
            toolName = "read_file",
            arguments = mapOf("path" to "/test/file.txt")
        )
        
        val response = McpToolResponse(
            success = true,
            result = "파일 내용입니다."
        )
        
        whenever(mcpService.executeTool(any<String>(), any())).thenReturn(response)

        // When
        val result = mcpController.executeTool(request)

        // Then
        assert(result.success)
        assert(result.result == "파일 내용입니다.")
        verify(mcpService).executeTool(eq("read_file"), any())
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
        
        whenever(mcpService.getServer("server-1")).thenReturn(server)

        // When
        val result = mcpController.getServerStatus("server-1").block()

        // Then
        assert(result != null)
        assert(result!!["id"] == "server-1")
        assert(result["name"] == "파일 시스템 서버")
        assert(result["enabled"] == true)
        verify(mcpService).getServer("server-1")
    }

    @Test
    fun `MCP 서버 추가 테스트`() {
        // Given
        val server = McpServer(
            id = "new-server",
            name = "새로운 서버",
            description = "새로 추가된 서버",
            command = "node",
            enabled = true
        )

        // When
        val result = mcpController.addServer(server).block()

        // Then
        assert(result != null)
        assert(result!!["message"] == "Server added successfully")
        assert(result["id"] == "new-server")
        verify(mcpService).addServer(server)
    }

    @Test
    fun `MCP 서버 제거 테스트`() {
        // Given
        whenever(mcpService.removeServer("server-1")).thenReturn(true)

        // When
        val result = mcpController.removeServer("server-1").block()

        // Then
        assert(result != null)
        assert(result!!["message"] == "Server removed successfully")
        verify(mcpService).removeServer("server-1")
    }
}
