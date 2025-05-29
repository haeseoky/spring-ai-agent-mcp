package com.ocean.agent.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ocean.agent.domain.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import jakarta.annotation.PostConstruct

/**
 * MCP(Model Context Protocol) 서버 관리 서비스
 */
@Service
class McpService(
    private val objectMapper: ObjectMapper,
    @Value("\${app.mcp.config-path:mcp-servers.json}") private val configPath: String
) {
    
    private val servers = ConcurrentHashMap<String, McpServer>()
    private val serverStatuses = ConcurrentHashMap<String, McpServerStatusInfo>()
    
    @PostConstruct
    fun initialize() {
        loadServersFromConfig()
        initializeDefaultServers()
    }
    
    /**
     * 설정 파일에서 MCP 서버 목록 로드
     */
    private fun loadServersFromConfig() {
        try {
            val configFile = File(configPath)
            if (configFile.exists()) {
                val serverList: List<McpServer> = objectMapper.readValue(configFile)
                serverList.forEach { server ->
                    servers[server.id] = server
                    serverStatuses[server.id] = McpServerStatusInfo(
                        serverId = server.id,
                        status = if (server.enabled) McpServerStatus.STOPPED else McpServerStatus.STOPPED
                    )
                }
            }
        } catch (e: Exception) {
            println("Failed to load MCP servers from config: ${e.message}")
        }
    }
    
    /**
     * 기본 MCP 서버들 초기화
     */
    private fun initializeDefaultServers() {
        if (servers.isEmpty()) {
            // 파일 시스템 도구
            val fileSystemServer = McpServer(
                id = "filesystem",
                name = "File System",
                description = "파일 시스템 작업을 위한 도구들",
                command = "node",
                args = listOf("filesystem-server.js"),
                tools = listOf(
                    McpTool(
                        name = "read_file",
                        description = "파일 내용을 읽습니다",
                        inputSchema = McpToolSchema(
                            properties = mapOf(
                                "path" to McpPropertySchema("string", "읽을 파일의 경로")
                            ),
                            required = listOf("path")
                        ),
                        serverId = "filesystem"
                    ),
                    McpTool(
                        name = "write_file",
                        description = "파일에 내용을 씁니다",
                        inputSchema = McpToolSchema(
                            properties = mapOf(
                                "path" to McpPropertySchema("string", "쓸 파일의 경로"),
                                "content" to McpPropertySchema("string", "파일에 쓸 내용")
                            ),
                            required = listOf("path", "content")
                        ),
                        serverId = "filesystem"
                    )
                )
            )
            
            // 웹 검색 도구
            val webSearchServer = McpServer(
                id = "web-search",
                name = "Web Search",
                description = "웹 검색 기능을 제공하는 도구들",
                command = "python",
                args = listOf("web-search-server.py"),
                tools = listOf(
                    McpTool(
                        name = "search_web",
                        description = "웹에서 정보를 검색합니다",
                        inputSchema = McpToolSchema(
                            properties = mapOf(
                                "query" to McpPropertySchema("string", "검색할 쿼리"),
                                "limit" to McpPropertySchema("integer", "결과 개수 제한")
                            ),
                            required = listOf("query")
                        ),
                        serverId = "web-search"
                    )
                )
            )
            
            addServer(fileSystemServer)
            addServer(webSearchServer)
        }
    }
    
    /**
     * 모든 MCP 서버 목록 반환
     */
    fun getAllServers(): List<McpServer> = servers.values.toList()
    
    /**
     * 활성화된 MCP 서버 목록 반환
     */
    fun getEnabledServers(): List<McpServer> = servers.values.filter { it.enabled }
    
    /**
     * 특정 MCP 서버 조회
     */
    fun getServer(id: String): McpServer? = servers[id]
    
    /**
     * MCP 서버 추가
     */
    fun addServer(server: McpServer) {
        servers[server.id] = server
        serverStatuses[server.id] = McpServerStatusInfo(
            serverId = server.id,
            status = McpServerStatus.STOPPED
        )
        saveServersToConfig()
    }
    
    /**
     * MCP 서버 제거
     */
    fun removeServer(id: String): Boolean {
        val removed = servers.remove(id) != null
        serverStatuses.remove(id)
        if (removed) {
            saveServersToConfig()
        }
        return removed
    }
    
    /**
     * MCP 서버 상태 업데이트
     */
    fun updateServerStatus(id: String, status: McpServerStatus) {
        serverStatuses[id] = McpServerStatusInfo(
            serverId = id,
            status = status,
            lastUpdated = java.time.LocalDateTime.now()
        )
    }
    
    /**
     * 모든 사용 가능한 도구 목록 반환
     */
    fun getAllTools(): List<McpTool> {
        return getEnabledServers().flatMap { it.tools }
    }
    
    /**
     * 특정 도구 실행
     */
    suspend fun executeTool(toolName: String, arguments: Map<String, Any>): McpToolResponse {
        val tool = getAllTools().find { it.name == toolName }
            ?: return McpToolResponse(false, error = "Tool not found: $toolName")
        
        return try {
            // 실제 MCP 서버와 통신하는 로직이 들어갈 곳
            // 지금은 시뮬레이션된 응답 반환
            when (toolName) {
                "read_file" -> simulateReadFile(arguments)
                "write_file" -> simulateWriteFile(arguments)
                "search_web" -> simulateWebSearch(arguments)
                else -> McpToolResponse(false, error = "Tool not implemented: $toolName")
            }
        } catch (e: Exception) {
            McpToolResponse(false, error = "Tool execution failed: ${e.message}")
        }
    }
    
    /**
     * 설정을 파일에 저장
     */
    private fun saveServersToConfig() {
        try {
            val configFile = File(configPath)
            objectMapper.writeValue(configFile, servers.values.toList())
        } catch (e: Exception) {
            println("Failed to save MCP servers to config: ${e.message}")
        }
    }
    
    // 시뮬레이션 메소드들 (실제 구현에서는 MCP 프로토콜로 통신)
    private fun simulateReadFile(args: Map<String, Any>): McpToolResponse {
        val path = args["path"] as? String ?: return McpToolResponse(false, error = "Path required")
        return try {
            val content = File(path).readText()
            McpToolResponse(true, result = content)
        } catch (e: Exception) {
            McpToolResponse(false, error = "Failed to read file: ${e.message}")
        }
    }
    
    private fun simulateWriteFile(args: Map<String, Any>): McpToolResponse {
        val path = args["path"] as? String ?: return McpToolResponse(false, error = "Path required")
        val content = args["content"] as? String ?: return McpToolResponse(false, error = "Content required")
        return try {
            File(path).writeText(content)
            McpToolResponse(true, result = "File written successfully")
        } catch (e: Exception) {
            McpToolResponse(false, error = "Failed to write file: ${e.message}")
        }
    }
    
    private fun simulateWebSearch(args: Map<String, Any>): McpToolResponse {
        val query = args["query"] as? String ?: return McpToolResponse(false, error = "Query required")
        val limit = (args["limit"] as? Number)?.toInt() ?: 5
        
        // 시뮬레이션된 검색 결과
        val results = listOf(
            mapOf("title" to "Sample Result 1", "url" to "https://example1.com", "snippet" to "Sample snippet 1"),
            mapOf("title" to "Sample Result 2", "url" to "https://example2.com", "snippet" to "Sample snippet 2")
        ).take(limit)
        
        return McpToolResponse(true, result = results)
    }
}
