package com.ocean.agent.controller

import com.ocean.agent.domain.*
import com.ocean.agent.service.McpService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * MCP 서버 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = ["*"]) // 개발용, 운영시에는 특정 도메인으로 제한
class McpController(
    private val mcpService: McpService
) {
    
    /**
     * 모든 MCP 서버 목록 조회
     */
    @GetMapping("/servers")
    fun getAllServers(): Mono<List<McpServer>> {
        return Mono.just(mcpService.getAllServers())
    }
    
    /**
     * 활성화된 MCP 서버 목록 조회
     */
    @GetMapping("/servers/enabled")
    fun getEnabledServers(): Mono<List<McpServer>> {
        return Mono.just(mcpService.getEnabledServers())
    }
    
    /**
     * 특정 MCP 서버 조회
     */
    @GetMapping("/servers/{id}")
    fun getServer(@PathVariable id: String): Mono<McpServer> {
        val server = mcpService.getServer(id)
        return if (server != null) {
            Mono.just(server)
        } else {
            Mono.error(RuntimeException("Server not found: $id"))
        }
    }
    
    /**
     * MCP 서버 추가
     */
    @PostMapping("/servers")
    fun addServer(@RequestBody server: McpServer): Mono<Map<String, String>> {
        mcpService.addServer(server)
        return Mono.just(mapOf("message" to "Server added successfully", "id" to server.id))
    }
    
    /**
     * MCP 서버 제거
     */
    @DeleteMapping("/servers/{id}")
    fun removeServer(@PathVariable id: String): Mono<Map<String, String>> {
        val removed = mcpService.removeServer(id)
        return if (removed) {
            Mono.just(mapOf("message" to "Server removed successfully"))
        } else {
            Mono.error(RuntimeException("Server not found: $id"))
        }
    }
    
    /**
     * 모든 사용 가능한 도구 목록 조회
     */
    @GetMapping("/tools")
    fun getAllTools(): Mono<List<McpTool>> {
        return Mono.just(mcpService.getAllTools())
    }
    
    /**
     * 특정 도구 실행
     */
    @PostMapping("/tools/execute")
    suspend fun executeTool(@RequestBody request: McpToolRequest): McpToolResponse {
        return mcpService.executeTool(request.toolName, request.arguments)
    }
    
    /**
     * MCP 서버 상태 조회
     */
    @GetMapping("/servers/{id}/status")
    fun getServerStatus(@PathVariable id: String): Mono<Map<String, Any>> {
        val server = mcpService.getServer(id)
        return if (server != null) {
            Mono.just(mapOf(
                "id" to id,
                "name" to server.name,
                "enabled" to server.enabled,
                "toolCount" to server.tools.size
            ))
        } else {
            Mono.error(RuntimeException("Server not found: $id"))
        }
    }
    
    /**
     * MCP 설정 예제 반환
     */
    @GetMapping("/example-config")
    fun getExampleConfig(): Mono<McpServer> {
        val exampleServer = McpServer(
            id = "example-server",
            name = "Example MCP Server",
            description = "예제 MCP 서버 설정",
            command = "node",
            args = listOf("server.js"),
            env = mapOf("API_KEY" to "your-api-key"),
            enabled = false,
            tools = listOf(
                McpTool(
                    name = "example_tool",
                    description = "예제 도구",
                    inputSchema = McpToolSchema(
                        properties = mapOf(
                            "input" to McpPropertySchema("string", "입력 값"),
                            "option" to McpPropertySchema("boolean", "옵션 값")
                        ),
                        required = listOf("input")
                    ),
                    serverId = "example-server"
                )
            )
        )
        return Mono.just(exampleServer)
    }
    
    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    fun health(): Mono<Map<String, Any>> {
        val enabledServers = mcpService.getEnabledServers()
        val totalTools = mcpService.getAllTools()
        
        return Mono.just(mapOf(
            "status" to "OK",
            "service" to "McpService",
            "enabledServers" to enabledServers.size,
            "totalTools" to totalTools.size,
            "servers" to enabledServers.map { mapOf("id" to it.id, "name" to it.name) }
        ))
    }
}
