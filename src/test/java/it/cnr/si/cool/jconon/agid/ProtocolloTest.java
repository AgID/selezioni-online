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

package it.cnr.si.cool.jconon.agid;

import it.almaviva.eprot.ResponseType;
import it.cnr.si.cool.jconon.agid.config.AGIDLoginConfiguration;
import it.cnr.si.cool.jconon.agid.config.ProtocolloClient;
import it.cnr.si.cool.jconon.agid.config.ProtocolloConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AGIDLoginConfiguration.class, ProtocolloConfiguration.class})
@ActiveProfiles("agid")
public class ProtocolloTest {
    @Autowired
    private ProtocolloClient protocolloClient;

    @Test
    public void protocolla() {
        final ResponseType responseType = protocolloClient.protocolla(
                "TEST",
                "MARIO ROSSI",
                "Domanda.txt",
                "DOMANDA DI CONCORSO".getBytes(StandardCharsets.UTF_8)
        );
        assertEquals("0000 - Protocollo Registrato correttamente. ", responseType.getDescrizioneEsito());
        assertEquals("0000", responseType.getEsito());
        assertNotNull(responseType.getNumeroProtocollo().getValue());
        assertNotNull(responseType.getDataProtocollo().getValue());
    }
}
