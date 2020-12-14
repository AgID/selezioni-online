package it.cnr.si.cool.jconon.agid.repository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessToken {
    private String access_token;
    private Integer expires_in;
    private String id_token;
    private String scope;
    private String token_type;
}
