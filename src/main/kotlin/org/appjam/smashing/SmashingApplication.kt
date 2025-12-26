package org.appjam.smashing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SmashingApplication

fun main(args: Array<String>) {
    runApplication<SmashingApplication>(*args)
}
