package no.authdemo.authdemo.security

import no.authdemo.authdemo.config.AppProperties
import no.authdemo.authdemo.util.CookieUtils.getCookie
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder.getContext
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils.hasText
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenAuthenticationFilter @Autowired internal constructor(
    private val appProperties: AppProperties,
) : OncePerRequestFilter() {
    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Autowired
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            if (hasText(jwt) && tokenProvider.validateToken(jwt)) {
                val userId = tokenProvider.getUserIdFromToken(jwt)
                val userDetails = customUserDetailsService.loadUserById(userId)
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            Companion.logger.error("Could not set user authentication in security context", ex)
        }
        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        var bearerToken =
            if(getCookie(request, appProperties.auth.tokenCookie).isPresent) {
                "Bearer " + getCookie(request, appProperties.auth.tokenCookie).get().value
            } else {
                request.getHeader("Authorization")
            }

        return if (hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7, bearerToken.length)
        } else null
    }

    companion object {
        private val logger = getLogger(TokenAuthenticationFilter::class.java)
    }
}