package org.appjam.smashing.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsUtils

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            httpBasic { disable() }
            formLogin { disable() }

            csrf { disable() }
            cors { }

            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }

            authorizeHttpRequests {
                authorize(CorsUtils::isPreFlightRequest, permitAll)

                PERMIT_ALL.forEach { authorize(it, permitAll) }

                authorize(anyRequest, permitAll) // TODO: 추후 로그인 기능 구현 시 수정
            }
        }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    companion object {
        private val PERMIT_ALL = arrayOf(
            // Swagger UI
            "/swagger-ui/**",
            "/swagger-ui.html",

            // Actuator
            "/actuator/**",
        )
    }
}
