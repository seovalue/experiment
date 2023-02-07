package com.example.experiment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExperimentApplication

fun main(args: Array<String>) {
	runApplication<ExperimentApplication>(*args)
}
