package it.cnr.si.cool.jconon.agid.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class AccessToken {
    private String access_token;
    private Integer expires_in;
    private String id_token;
    private String scope;
    private String token_type;
}
