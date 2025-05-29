package com.ocean.agent.domain

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * MCP 서버 정보를 표현하는 도메인 모델
 */
data class McpServer(
    val id: String,
    val name: String,
    val description: String,
    val command: String,
    val args: List<String> = emptyList(),
    val env: Map<String, String> = emptyMap(),
    val enabled: Boolean = true,
    val tools: List<McpTool> = emptyList()
)

/**
 * MCP 도구 정보를 표현하는 도메인 모델
 */
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: McpToolSchema,
    val serverId: String
)

/**
 * MCP 도구 스키마
 */
data class McpToolSchema(
    val type: String = "object",
    val properties: Map<String, McpPropertySchema> = emptyMap(),
    val required: List<String> = emptyList()
)

/**
 * MCP 속성 스키마
 */
data class McpPropertySchema(
    val type: String,
    val description: String,
    val enum: List<String>? = null,
    val default: Any? = null
)

/**
 * MCP 서버 상태
 */
enum class McpServerStatus {
    RUNNING,
    STOPPED,
    ERROR,
    CONNECTING
}

/**
 * MCP 서버 상태 정보
 */
data class McpServerStatusInfo(
    val serverId: String,
    val status: McpServerStatus,
    val message: String? = null,
    val lastUpdated: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

/**
 * MCP 도구 실행 요청
 */
data class McpToolRequest(
    val toolName: String,
    val arguments: Map<String, Any>
)

/**
 * MCP 도구 실행 응답
 */
data class McpToolResponse(
    val success: Boolean,
    val result: Any? = null,
    val error: String? = null
)
