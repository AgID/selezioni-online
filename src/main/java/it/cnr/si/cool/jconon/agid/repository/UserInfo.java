package it.cnr.si.cool.jconon.agid.repository;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class UserInfo {
    private String sub;
    private String email;
    private String firstname;
    private String lastname;
    private String fiscalNumber;
    private String phone;

}
