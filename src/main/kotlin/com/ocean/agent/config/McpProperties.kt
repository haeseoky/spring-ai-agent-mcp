package com.ocean.agent.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * MCP 관련 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "app.mcp")
data class McpProperties(
    /**
     * MCP 서버 설정 파일 경로
     */
    var configPath: String = "mcp-servers.json",
    
    /**
     * MCP 기능 활성화 여부
     */
    var enabled: Boolean = true
)
