package no.authdemo.authdemo.service

import no.authdemo.authdemo.config.AppProperties
import no.authdemo.authdemo.exception.BadRequestException
import no.authdemo.authdemo.model.AuthProvider
import no.authdemo.authdemo.model.User
import no.authdemo.authdemo.repository.UserRepository
import no.authdemo.authdemo.security.TokenProvider
import no.authdemo.authdemo.util.CookieUtils.addCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpServletResponse
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@Service
class AuthService(
        val authenticationManager: AuthenticationManager,
        val tokenProvider: TokenProvider,
        val appProperties: AppProperties,
        val userRepository: UserRepository,
        val passwordEncoder: PasswordEncoder
) {
    fun handleLogin(email: @NotBlank @Email String, password: @NotBlank String, response: HttpServletResponse) : String {
        val authentication = createAuthentication(email, password)
        setCurrentlyAuthenticatedUser(authentication)

        val token = tokenProvider.createToken(authentication)
        addCookie(response, appProperties.auth.tokenCookie, token)

        return token
    }

    fun handleRegistration(name: @NotBlank String, email: @NotBlank @Email String, password: @NotBlank String): URI {
        if (userRepository.existsByEmail(email))
            throw BadRequestException("Email address already in use.")

        val user = createUser(name, email, password)
        val registeredUser = userRepository.save(user)

        return getUserInfoUri(registeredUser)
    }

    private fun createAuthentication(email: String?, password: String?): Authentication {
        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
    }

    private fun setCurrentlyAuthenticatedUser(authentication: Authentication) {
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun getUserInfoUri(result: User) = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/user/me")
            .buildAndExpand(result.id).toUri()

    private fun createUser(name : String, email : String, password : String): User {
        val user = User()
        user.name = name
        user.email = email
        user.password = password
        user.provider = AuthProvider.local
        user.password = passwordEncoder.encode(user.password)
        return user
    }
}
