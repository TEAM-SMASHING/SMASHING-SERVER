package org.appjam.smashing.global.config

import org.appjam.smashing.global.auth.jwt.filter.JwtAuthenticationFilter
import org.appjam.smashing.global.auth.jwt.handler.JwtAccessDeniedHandler
import org.appjam.smashing.global.auth.jwt.handler.JwtAuthenticationEntryPoint
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsUtils

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

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

                authorize(anyRequest, authenticated)
            }

            exceptionHandling {
                authenticationEntryPoint = jwtAuthenticationEntryPoint
                accessDeniedHandler = jwtAccessDeniedHandler
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtAuthenticationFilterRegistration(
        filter: JwtAuthenticationFilter
    ): FilterRegistrationBean<JwtAuthenticationFilter> =
        FilterRegistrationBean(filter).apply {
            isEnabled = false
        }

    companion object {
        private val PERMIT_ALL = arrayOf(
            // Swagger UI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",

            // Actuator
            "/actuator/**",

            // API
            "/api/v1/auth/login/kakao",
            "/api/v1/auth/signup",
            "/api/v1/users/nickname-availability",
            "/api/v1/users/openchat/validate",
        )
    }
}
