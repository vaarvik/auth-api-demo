package no.authdemo.authdemo.controller

import no.authdemo.authdemo.exception.ResourceNotFoundException
import no.authdemo.authdemo.model.User
import no.authdemo.authdemo.repository.UserRepository
import no.authdemo.authdemo.security.CurrentUser
import no.authdemo.authdemo.security.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    fun getCurrentUser(@CurrentUser userPrincipal: UserPrincipal): User {
        return userRepository.findById(userPrincipal.id!!)
            .orElseThrow { ResourceNotFoundException("User", "id", userPrincipal.id) }
    }
}