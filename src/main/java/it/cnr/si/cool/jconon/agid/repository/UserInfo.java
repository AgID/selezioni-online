package it.cnr.si.cool.jconon.agid.repository;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfo {
    private String sub;
    private String email;
    private String firstname;
    private String lastname;
    private String fiscalNumber;
    private String phone;

}
