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

import it.almaviva.eprot.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.bind.JAXBElement;

public class ProtocolloClient extends WebServiceGatewaySupport {
    private final String aoo;
    private final String ufficio;
    private final String utente;
    private final String password;
    private final String tipoProtocollo;
    private final String tipoDocumento;

    public ProtocolloClient(String aoo, String ufficio, String utente, String password, String tipoProtocollo, String tipoDocumento) {
        this.aoo = aoo;
        this.ufficio = ufficio;
        this.utente = utente;
        this.password = password;
        this.tipoProtocollo = tipoProtocollo;
        this.tipoDocumento = tipoDocumento;
    }

    public ResponseType protocolla(String oggetto, String mittente, String nomeFile, byte[] file) {
        ObjectFactory objectFactory = new ObjectFactory();
        RegistraRequestType request = objectFactory.createRegistraRequestType();
        final IdentificazioneType identificazioneType = objectFactory.createIdentificazioneType();
        identificazioneType.setAoo(aoo);
        identificazioneType.setUfficio(ufficio);
        identificazioneType.setUtente(utente);
        identificazioneType.setPassword(password);
        request.setIdentificazione(identificazioneType);

        final ProtocolloType protocolloType = objectFactory.createProtocolloType();
        protocolloType.setTipoProtocollo(tipoProtocollo);
        protocolloType.setOggetto(oggetto);
        protocolloType.setMittente(mittente);
        protocolloType.setTipoDocumento(tipoDocumento);
        request.setProtocollo(protocolloType);

        final ProtocolloType.Documenti documenti = objectFactory.createProtocolloTypeDocumenti();
        final DocumentoType documentoType = objectFactory.createDocumentoType();
        documentoType.setTipoDocumento(tipoDocumento);
        documentoType.setNomeFile(nomeFile);
        documentoType.setFileBase64(file);

        documenti.setDocumento(documentoType);
        request.getProtocollo().getDocumenti().add(
                documenti
        );
        final JAXBElement<RegistraRequestType> registraProtocolloRequest =
                objectFactory.createRegistraProtocolloRequest(request);

        JAXBElement<ResponseType> response = (JAXBElement<ResponseType>) getWebServiceTemplate()
                .marshalSendAndReceive(registraProtocolloRequest);
        return response.getValue();
    }
}
