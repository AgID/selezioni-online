package it.cnr.si.cool.jconon.agid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agid-login")
@Getter
@Setter
public class AGIDLoginConfigurationProperties {
    private String name;
    private String url;
    private String auth;
    private String logout;
    private String redirect_uri;
    private String post_logout_redirect_uri;
    private String client_id;
    private String client_secret;
    private String response_type;
    private String scope;
}
