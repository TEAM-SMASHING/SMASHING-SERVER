package org.appjam.smashing.global.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "37-APPJAM-SERVER-SMASHING API",
        version = "1.0",
        description = "DIVE SOPT 37기 앱잼 스매싱 서버 SWAGGER입니다."
    )
)
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val localServer = Server()
            .url("http://localhost:8080")
            .description("Local Server")

        val devServer = Server()
            .url("http://15.164.76.231:8080")
            .description("Dev Server")

        return OpenAPI()
            .servers(listOf(localServer, devServer))
    }
}
