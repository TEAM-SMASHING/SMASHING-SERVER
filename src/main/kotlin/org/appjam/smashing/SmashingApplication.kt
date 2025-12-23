package org.appjam.smashing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmashingApplication

fun main(args: Array<String>) {
	runApplication<SmashingApplication>(*args)
}
