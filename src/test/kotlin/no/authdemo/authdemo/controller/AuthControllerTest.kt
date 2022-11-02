package no.authdemo.authdemo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

}