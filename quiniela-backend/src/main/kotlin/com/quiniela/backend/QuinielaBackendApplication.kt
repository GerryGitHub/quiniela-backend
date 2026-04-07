package com.quiniela.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class QuinielaBackendApplication

fun main(args: Array<String>) {
    SpringApplication.run(QuinielaBackendApplication::class.java, *args)
}
