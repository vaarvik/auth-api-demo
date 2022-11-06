package no.authdemo.authdemo.controller

import no.authdemo.authdemo.config.AppProperties
import no.authdemo.authdemo.exception.BadRequestException
import no.authdemo.authdemo.model.AuthProvider
import no.authdemo.authdemo.model.User
import no.authdemo.authdemo.payload.ApiResponse
import no.authdemo.authdemo.payload.AuthResponse
import no.authdemo.authdemo.payload.LoginRequest
import no.authdemo.authdemo.payload.SignUpRequest
import no.authdemo.authdemo.repository.UserRepository
import no.authdemo.authdemo.security.TokenProvider
import no.authdemo.authdemo.util.CookieUtils.addCookie
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(private val appProperties: AppProperties) {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @PostMapping("/login")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequest?, response: HttpServletResponse): ResponseEntity<*> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest!!.email,
                loginRequest.password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        val token = tokenProvider.createToken(authentication)
        addCookie(response, appProperties.auth.tokenCookie, token)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignUpRequest?): ResponseEntity<*> {
        if (userRepository.existsByEmail(signUpRequest!!.email!!)) {
            throw BadRequestException("Email address already in use.")
        }

        // Creating user's account
        val user = User()
        user.name = signUpRequest.name
        user.email = signUpRequest.email
        user.password = signUpRequest.password
        user.provider = AuthProvider.local
        user.password = passwordEncoder.encode(user.password)
        val result = userRepository.save(user)
        val location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/user/me")
            .buildAndExpand(result.id).toUri()
        return ResponseEntity.created(location)
            .body(ApiResponse(true, "User registered successfully@"))
    }
}