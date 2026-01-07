package org.appjam.smashing.global.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients("org.appjam.smashing")
class FeignConfig {
}
