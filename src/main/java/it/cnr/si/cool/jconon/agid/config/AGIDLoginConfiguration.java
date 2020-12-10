package it.cnr.si.cool.jconon.agid.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "oil.url")
@EnableConfigurationProperties(AGIDLoginConfigurationProperties.class)
public class AGIDLoginConfiguration {
}
