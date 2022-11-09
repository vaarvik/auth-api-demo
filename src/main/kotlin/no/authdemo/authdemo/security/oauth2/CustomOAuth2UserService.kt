package no.authdemo.authdemo.security.oauth2

import no.authdemo.authdemo.exception.OAuth2AuthenticationProcessingException
import no.authdemo.authdemo.model.AuthProvider
import no.authdemo.authdemo.model.User
import no.authdemo.authdemo.repository.UserRepository
import no.authdemo.authdemo.security.UserPrincipal
import no.authdemo.authdemo.security.oauth2.user.OAuth2UserInfo
import no.authdemo.authdemo.security.oauth2.user.OAuth2UserInfoFactory
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasLength

@Service
class CustomOAuth2UserService(val userRepository: UserRepository) : DefaultOAuth2UserService() {

    // Think of this method as "onLoadOAuth2User"
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(oAuth2UserRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(oAuth2UserRequest)

        return try {
            val oAuth2ProviderSlug = oAuth2UserRequest.clientRegistration.registrationId
            handleReceivedOAuth2User(oAuth2ProviderSlug, oAuth2User)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun handleReceivedOAuth2User(oAuth2ProviderSlug: String, oAuth2User: OAuth2User): OAuth2User {
        val oAuth2UserInfo = getOAuth2UserInfoFromProvider(oAuth2ProviderSlug, oAuth2User)

        if(!hasLength(oAuth2UserInfo.email))
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")

        val potentialUser = userRepository.findByEmail(oAuth2UserInfo.email)
        var user: User

        if (potentialUser.isPresent) {
            user = potentialUser.get()
            validateProviderUsedByExistingUser(oAuth2ProviderSlug, user)
            user = updateExistingUser(user, oAuth2UserInfo)
        } else {
            user = registerNewUser(oAuth2ProviderSlug, oAuth2UserInfo)
        }

        return UserPrincipal.create(user, oAuth2User.attributes)
    }

    private fun validateProviderUsedByExistingUser(
        oAuth2ProviderSlug: String,
        user: User
    ) {
        val requestedProvider = AuthProvider.valueOf(oAuth2ProviderSlug)
        if (user.provider != requestedProvider) {
            throw OAuth2AuthenticationProcessingException(
                "Looks like you're signed up with " +
                        user.provider + " account. Please use your " + user.provider +
                        " account to login."
            )
        }
    }

    private fun getOAuth2UserInfoFromProvider(oAuth2ProviderSlug: String, oAuth2User: OAuth2User)
        = OAuth2UserInfoFactory.getOAuth2UserInfo(
            oAuth2ProviderSlug,
            oAuth2User.attributes
    )

    private fun registerNewUser(oAuth2ProviderSlug: String, oAuth2UserInfo: OAuth2UserInfo): User {
        val user = createOAuth2User(oAuth2ProviderSlug, oAuth2UserInfo)
        return userRepository.save(user)
    }

    private fun createOAuth2User(oAuth2ProviderSlug: String,
                                 oAuth2UserInfo: OAuth2UserInfo): User {
        val user = User()
        val requestedProvider = AuthProvider.valueOf(oAuth2ProviderSlug)
        user.provider = requestedProvider
        user.providerId = oAuth2UserInfo.id
        user.name = oAuth2UserInfo.name
        user.email = oAuth2UserInfo.email
        user.imageUrl = oAuth2UserInfo.imageUrl
        return user
    }

    private fun updateExistingUser(existingUser: User, oAuth2UserInfo: OAuth2UserInfo): User {
        existingUser.name = oAuth2UserInfo.name
        existingUser.imageUrl = oAuth2UserInfo.imageUrl
        return userRepository.save(existingUser)
    }
}
