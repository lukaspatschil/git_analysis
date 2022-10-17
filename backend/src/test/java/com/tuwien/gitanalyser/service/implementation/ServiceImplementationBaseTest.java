package com.tuwien.gitanalyser.service.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import utils.Randoms;

public abstract class ServiceImplementationBaseTest {

    protected ClientRegistration getRandomAuthorization() {
        return ClientRegistration.withRegistrationId(Randoms.alpha()).clientId(Randoms.alpha()).clientSecret(Randoms.alpha())
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(Randoms.alpha()).scope(Randoms.alpha()).authorizationUri(Randoms.alpha())
                                 .tokenUri(Randoms.alpha()).userInfoUri(Randoms.alpha()).userNameAttributeName("id")
                                 .clientName(Randoms.alpha()).build();
    }
}
