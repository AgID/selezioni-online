package it.cnr.si.cool.jconon.agid.repository;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestHeader;

@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface AGIDLogin {
    String AUTH_TOKEN = "Authorization";

    @RequestLine("POST /token")
    AccessToken getTokenFull(
            @Param("grant_type") String grantType,
            @Param("code") String code,
            @Param("redirect_uri") String redirectUri
    );

    @RequestLine("POST /userinfo")
    UserInfo getUserInfo(@RequestHeader(AUTH_TOKEN) String bearerToken);

}
