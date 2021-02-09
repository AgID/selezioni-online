package it.cnr.si.cool.jconon.agid.repository;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({"Content-Type: application/x-www-form-urlencoded"})
public interface AGIDLogin {

    @RequestLine("POST /token")
    AccessToken getTokenFull(
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

    @Headers("Authorization: {token}")
    @RequestLine("GET /session/end")
    void logout(
            @Param("id_token") String id_token,
            @Param("client_id") String client_id,
            @Param("client_secret") String client_secret
    );

}
