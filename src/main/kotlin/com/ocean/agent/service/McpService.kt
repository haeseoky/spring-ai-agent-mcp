package com.ocean.agent.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Service
class McpService(
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("http://localhost:8000")
        .build()
) {

    /**
     * MCP 시간 서버에서 현재 시간 조회
     */
    fun getCurrentTime(): Mono<TimeResponse> {
        return webClient.get()
            .uri("/get_current_time")
            .retrieve()
            .bodyToMono(TimeResponse::class.java)
    }

    /**
     * MCP 서버의 사용 가능한 도구 목록 조회
     */
    fun getAvailableTools(): Mono<String> {
        return webClient.get()
            .uri("/docs")
            .retrieve()
            .bodyToMono(String::class.java)
    }

    /**
     * 특정 날짜/시간 정보 조회
     */
    fun getTimeInfo(timezone: String = "Asia/Seoul"): Mono<TimeResponse> {
        return webClient.get()
            .uri { builder ->
                builder.path("/get_current_time")
                    .queryParam("timezone", timezone)
                    .build()
            }
            .retrieve()
            .bodyToMono(TimeResponse::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimeResponse(
    val currentTime: String? = null,
    val timezone: String? = null,
    val timestamp: Long? = null,
    val error: String? = null
)
