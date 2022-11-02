package no.authdemo.authdemo

import no.authdemo.authdemo.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AuthDemoApplication

fun main(args: Array<String>) {
    runApplication<AuthDemoApplication>(*args)
}