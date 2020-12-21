package it.cnr.si.cool.jconon.agid.repository;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestHeader;

@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface AGIDLogin {

    @RequestLine("POST /token")
    @Headers("Authorization: {basicAuth}")
    AccessToken getTokenFull(
            @Param("basicAuth") String basicAuth,
            @Param("grant_type") String grantType,
            @Param("code") String code,
            @Param("redirect_uri") String redirectUri,
            @Param("client_id") String client_id,
            @Param("client_secret") String client_secret
    );

    @Headers("Authorization: {token}")
    @RequestLine("POST /userinfo")
    UserInfo getUserInfo(
            @Param("token") String token,
            @Param("client_id") String client_id,
            @Param("client_secret") String client_secret
    );

}
