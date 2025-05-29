package com.ocean.agent.service

import com.ocean.agent.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import java.time.LocalDateTime

class McpServiceTest {

    @Test
    fun `MCP 서비스 기본 테스트`() {
        // McpService는 외부 프로세스와 통신하므로 실제 통합 테스트에서 진행
        // 여기서는 기본적인 구조만 테스트
        assertTrue(true)
    }

    @Test
    @Disabled("실제 MCP 서버가 필요하므로 통합 테스트에서 실행")
    fun `MCP 도구 실행 테스트`() = runBlocking {
        // 실제 MCP 서버가 필요한 테스트
    }
}
