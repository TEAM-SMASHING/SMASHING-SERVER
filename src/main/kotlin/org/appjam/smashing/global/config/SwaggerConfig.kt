package org.appjam.smashing.global.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "37-APPJAM-SERVER-SMASHING API",
        version = "1.0",
        description = """
            DIVE SOPT 37기 앱잼 스매싱 서버 SWAGGER입니다.
            swagger 토큰 입력의 경우 Bearer 를 제외하고 입력 부탁드립니다.
            """
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

        val bearerAuthScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")

        val securityRequirement = SecurityRequirement().addList(SECURITY_SCHEME_NAME)

        return OpenAPI()
            .servers(listOf(localServer, devServer))
            .components(Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuthScheme))
            .addSecurityItem(securityRequirement)
    }

    companion object {
        private const val SECURITY_SCHEME_NAME = "BearerAuth"
    }
}
