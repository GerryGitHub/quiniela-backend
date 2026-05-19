package com.quiniela.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.validation.annotation.Validated

@SpringBootApplication
@EnableScheduling
@Validated
open class QuinielaBackendApplication

fun main(args: Array<String>) {
    SpringApplication.run(QuinielaBackendApplication::class.java, *args)
}
