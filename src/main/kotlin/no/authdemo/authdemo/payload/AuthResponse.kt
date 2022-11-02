package no.authdemo.authdemo.payload

class AuthResponse(var accessToken: String) {
    var tokenType = "Bearer"
}