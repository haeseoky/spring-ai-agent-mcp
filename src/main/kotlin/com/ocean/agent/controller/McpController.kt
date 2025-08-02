package com.ocean.agent.controller

import com.ocean.agent.service.McpService
import com.ocean.agent.service.TimeResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/mcp")
class McpController(
    private val mcpService: McpService
) {

    /**
     * MCP 서버에서 현재 시간 조회
     */
    @GetMapping("/time")
    fun getCurrentTime(): Mono<TimeResponse> {
        return mcpService.getCurrentTime()
    }

    /**
     * 특정 타임존의 시간 조회
     */
    @GetMapping("/time/{timezone}")
    fun getTimeByTimezone(@PathVariable timezone: String): Mono<TimeResponse> {
        return mcpService.getTimeInfo(timezone)
    }

    /**
     * MCP 서버 상태 확인
     */
    @GetMapping("/health")
    fun checkMcpHealth(): Mono<Map<String, Any>> {
        return mcpService.getCurrentTime()
            .map { mapOf("status" to "UP", "mcpServer" to "CONNECTED", "data" to it) }
            .onErrorReturn(mapOf("status" to "DOWN", "mcpServer" to "DISCONNECTED"))
    }

    /**
     * 사용 가능한 MCP 도구 목록
     */
    @GetMapping("/tools")
    fun getAvailableTools(): Mono<String> {
        return mcpService.getAvailableTools()
    }
}
