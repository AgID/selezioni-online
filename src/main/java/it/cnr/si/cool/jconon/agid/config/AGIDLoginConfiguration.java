package it.cnr.si.cool.jconon.agid.config;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import it.cnr.si.cool.jconon.agid.repository.AGIDLogin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "agid-login.url")
@EnableConfigurationProperties(AGIDLoginConfigurationProperties.class)
public class AGIDLoginConfiguration {

    private final AGIDLoginConfigurationProperties properties;

    private AGIDLogin agidLogin;

    public AGIDLoginConfiguration(AGIDLoginConfigurationProperties agidLoginConfigurationProperties) {
        this.properties = agidLoginConfigurationProperties;
    }

    @Bean
    public AGIDLogin initAGIDLogin() {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(properties.getClient_id(), properties.getClient_secret()))
                .decoder(new GsonDecoder())
                .encoder(new FormEncoder(new GsonEncoder()))
                .target(AGIDLogin.class, properties.getUrl());
    }
}
