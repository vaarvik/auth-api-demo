package no.authdemo.authdemo.controller

import no.authdemo.authdemo.model.payload.ApiResponseDto
import no.authdemo.authdemo.model.payload.AuthResponseDto
import no.authdemo.authdemo.model.payload.LoginRequestDto
import no.authdemo.authdemo.model.payload.SignUpRequestDto
import no.authdemo.authdemo.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(val authService: AuthService) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequestDto, response: HttpServletResponse): ResponseEntity<*> {
        return try {
            val token = authService.handleLogin(
                    loginRequest.email,
                    loginRequest.password,
                    response
            )

            return ResponseEntity.ok(AuthResponseDto(token))
        } catch (error: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto(false, error.message!!))
        }
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignUpRequestDto): ResponseEntity<*> {
        println(signUpRequest)
        return try {
            val userInfoEndpoint = authService.handleRegistration(signUpRequest.name, signUpRequest.email, signUpRequest.password)

            ResponseEntity.created(userInfoEndpoint) // returns 201 with current user info endpoint in location in header
                    .body(ApiResponseDto(true, "User registered successfully."))
        } catch (error: RuntimeException) {
            ResponseEntity.badRequest().body(ApiResponseDto(false, error.message!!))
        }

    }

}
