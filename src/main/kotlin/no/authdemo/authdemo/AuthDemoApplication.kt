package no.authdemo.authdemo

import no.authdemo.authdemo.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AuthDemoApplication

fun main(args: Array<String>) {
    runApplication<AuthDemoApplication>(*args)
}