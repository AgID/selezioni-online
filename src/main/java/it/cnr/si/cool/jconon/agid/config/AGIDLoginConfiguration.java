package it.cnr.si.cool.jconon.agid.config;

import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import it.cnr.si.cool.jconon.agid.repository.AGIDLogin;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

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
                .retryer(Retryer.NEVER_RETRY)
                .client(new Client.Default(getSSLSocketFactory(), null))
                .decoder(new GsonDecoder())
                .encoder(new FormEncoder(new GsonEncoder()))
                .target(AGIDLogin.class, properties.getUrl());
    }

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            return sslContext.getSocketFactory();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
