package no.authdemo.authdemo.login.regular

import no.authdemo.authdemo.model.AuthProvider
import no.authdemo.authdemo.model.User
import no.authdemo.authdemo.repository.UserRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class RegularLoginTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    internal fun setup() {
        val user = User()
        user.id = 1
        user.name = "Local User"
        user.email = "local@mock.com"
        user.password = "localPw"
        user.password = passwordEncoder.encode(user.password)
        Mockito.`when`(userRepository.findByEmail("local@mock.com")).thenReturn(Optional.of(user))
    }

    @Test
    fun returnsTokenWithStatus200UponRequestWithValidCredentials() {
        mockMvc
            .perform(
                post("/auth/login")
                    .content("{\"email\": \"local@mock.com\", \"password\": \"localPw\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").exists())
    }
}