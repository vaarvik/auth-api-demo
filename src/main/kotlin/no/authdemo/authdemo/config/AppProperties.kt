package no.authdemo.authdemo.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app") // sets variables using attributes from "app" in application.yaml
class AppProperties {
    val auth = Auth()
    val oauth2 = OAuth2()

    class Auth {
        var tokenSecret: String? = null
        var tokenExpirationMsec: Long = 0
        var tokenCookie: String = "session_token"
    }

    class OAuth2 {
        var authorizedRedirectUris: List<String> = ArrayList()
            private set
    }
}