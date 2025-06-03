package com.ocean.agent.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

/**
 * WebFlux 설정
 */
@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
    
    /**
     * CORS 설정
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:[*]", "http://127.0.0.1:[*]", "https://localhost:[*]")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)  // credentials를 false로 변경
    }
    
    /**
     * 정적 리소스 핸들러 설정
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
    }
    
    /**
     * 루트 경로를 index.html로 라우팅
     */
    @Bean
    fun indexRouter(): RouterFunction<ServerResponse> = router {
        GET("/") {
            ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(ClassPathResource("static/index.html"))
        }
    }
}
