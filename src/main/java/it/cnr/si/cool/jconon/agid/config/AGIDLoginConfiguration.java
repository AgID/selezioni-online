/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.agid.config;

import feign.Feign;
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
                .decoder(new GsonDecoder())
                .encoder(new FormEncoder(new GsonEncoder()))
                .target(AGIDLogin.class, properties.getUrl());
    }
}
