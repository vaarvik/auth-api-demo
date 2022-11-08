package no.authdemo.authdemo.model.payload

data class AuthResponseDto(var accessToken: String) {
    var tokenType = "Bearer"
}
