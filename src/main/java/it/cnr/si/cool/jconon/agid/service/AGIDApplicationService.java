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

package it.cnr.si.cool.jconon.agid.service;

import it.cnr.si.cool.jconon.service.application.ApplicationService;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class AGIDApplicationService extends ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDApplicationService.class);

    @Override
    protected void verificaAttivita(Boolean ctrlAlternativeAttivita, Boolean existVerificaAttivita,
                                    Boolean existRelazioneAttivita, Boolean existCurriculum,
                                    StringBuilder messageError, Folder application, Session cmisSession) {
        LOGGER.info("Activity verification is disabled");
    }
}
