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

import it.cnr.cool.mail.model.AttachmentBean;
import it.cnr.cool.mail.model.EmailMessage;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.si.cool.jconon.cmis.model.JCONONDocumentType;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPolicyType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.application.ApplicationService;
import it.cnr.si.cool.jconon.service.call.CallService;
import it.cnr.si.opencmis.criteria.Criteria;
import it.cnr.si.opencmis.criteria.CriteriaFactory;
import it.cnr.si.opencmis.criteria.restrictions.Restrictions;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Primary
@Service
public class AGIDCallService extends CallService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDCallService.class);

    @Value("${mail.protocol.to}")
    private String mailProtocol;

    @Override
    public void protocolApplication(Session session) {
        Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_CALL.queryName());
        criteria.add(
                Restrictions.ge(
                        JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value(),
                        LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME)
                )
        );
        criteria.add(
                Restrictions.le(
                        JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value(),
                        LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME)
                )
        );
        ItemIterable<QueryResult> bandi = criteria.executeQuery(session, false, session.getDefaultContext());
        for (QueryResult queryResult : bandi.getPage(Integer.MAX_VALUE)) {
            Folder call = (Folder) session.getObject((String) queryResult.getPropertyValueById(PropertyIds.OBJECT_ID));
            protocolApplication(session, call);
        }
    }

    public void protocolApplication(Session session, Folder call) {
        LOGGER.info("Start protocol application for call {}", call.getName());
        SecondaryType objectTypeProtocollo = (SecondaryType) session.getTypeDefinition(JCONONPolicyType.JCONON_PROTOCOLLO.value());
        ItemIterable<QueryResult> domande = getApplicationConfirmed(session, call);
        final long totalNumItems = domande.getTotalNumItems();
        if (totalNumItems != getTotalApplicationSend(call)) {
            mailService.sendErrorMessage("protocol", "ERROR SOLR", "For call " + call.getName());
        }
        if (totalNumItems != 0) {
            try {
                Iterator<CmisObject> iterator = call.getChildren().iterator();
                int characteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
                Spliterator<CmisObject> spliterator = Spliterators.spliteratorUnknownSize(iterator, characteristics);
                boolean parallel = false;
                Stream<CmisObject> children = StreamSupport.stream(spliterator, parallel);
                List<Folder> applications = children
                        .filter(cmisObject -> cmisObject.getType().getId().equals(JCONONFolderType.JCONON_APPLICATION.value()))
                        .filter(cmisObject -> cmisObject.getPropertyValue(
                                JCONONPropertyIds.APPLICATION_STATO_DOMANDA.value()).equals(ApplicationService.StatoDomanda.CONFERMATA.getValue()))
                        .filter(Folder.class::isInstance)
                        .map(Folder.class::cast)
                        .sorted(Comparator.comparing(folder -> folder.getPropertyValue(PropertyIds.LAST_MODIFICATION_DATE)))
                        .collect(Collectors.toList());

                for (Folder domanda : applications) {
                    final CMISUser cmisUser = userService.loadUserForConfirm(
                            domanda.getPropertyValue(JCONONPropertyIds.APPLICATION_USER.value())
                    );
                    List<SecondaryType> secondaryTypes = domanda.getSecondaryTypes();
                    if (secondaryTypes.contains(objectTypeProtocollo))
                        continue;
                    LOGGER.info("Start protocol application {} ", domanda.getName());
                    try {
                        Map<String, Object> properties = new HashMap<String, Object>();
                        List<String> secondaryTypesId = new ArrayList<String>();
                        for (SecondaryType secondaryType : secondaryTypes) {
                            secondaryTypesId.add(secondaryType.getId());
                        }
                        secondaryTypesId.add(objectTypeProtocollo.getId());
                        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypesId);
                        domanda.updateProperties(properties);
                        final Document printApplication = (Document) session.getObject(
                                competitionService.findAttachmentId(session, domanda.getId(), JCONONDocumentType.JCONON_ATTACHMENT_APPLICATION)
                        );
                        EmailMessage message = new EmailMessage();
                        message.setHtmlBody(Boolean.TRUE);
                        message.setRecipients(Collections.singletonList(mailProtocol));
                        message.setSubject(i18NService.getLabel(
                                "subject.protocol.application",
                                Locale.ITALIAN,
                                call.getPropertyValue(JCONONPropertyIds.CALL_CODICE.value()),
                                domanda.getPropertyValue(PropertyIds.NAME)
                                )
                        );
                        message.setBody(i18NService.getLabel(
                                "body.protocol.application",
                                Locale.ITALIAN,
                                domanda.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_NOME.value())
                                        .concat(" ")
                                        .concat(domanda.getPropertyValue(JCONONPropertyIds.APPLICATION_COGNOME.value())).toUpperCase(),
                                domanda.getPropertyValue(PropertyIds.OBJECT_ID)
                                )
                        );
                        message.setAttachments(Arrays.asList(new AttachmentBean(
                                printApplication.getName(),
                                IOUtils.toByteArray(printApplication.getContentStream().getStream())
                        )));
                        message.setSender(
                                Optional.ofNullable(domanda.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_EMAIL_COMUNICAZIONI.value()))
                                        .orElse(cmisUser.getEmail())
                        );
                        mailService.send(message);
                    } catch (Exception e) {
                        LOGGER.error("Cannot add protocol to application", e);
                    }
                }
            } catch (Exception _ex) {
                LOGGER.error("Cannot add protocol to application", _ex);
            }
        }
        LOGGER.info("End protocol application for call {}", call.getName());
    }

}
