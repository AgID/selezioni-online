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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@EnableConfigurationProperties(ProtocolloConfigurationProperties.class)
public class ProtocolloConfiguration {

    private final ProtocolloConfigurationProperties properties;

    public ProtocolloConfiguration(ProtocolloConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("it.almaviva.eprot");
        return marshaller;
    }

    @Bean
    public ProtocolloClient protocolloClient(Jaxb2Marshaller marshaller) {
        ProtocolloClient client = new ProtocolloClient(
                properties.getAoo(),
                properties.getUfficio(),
                properties.getUtente(),
                properties.getPassword(),
                properties.getTipoProtocollo(),
                properties.getTipoDocumentoProtocollo(),
                properties.getTipoDocumento()
        );
        client.setDefaultUri(properties.getUrl());
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}
