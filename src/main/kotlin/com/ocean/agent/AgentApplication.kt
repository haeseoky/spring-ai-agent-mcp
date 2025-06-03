package com.ocean.agent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AgentApplication

fun main(args: Array<String>) {
    runApplication<AgentApplication>(*args)
}
