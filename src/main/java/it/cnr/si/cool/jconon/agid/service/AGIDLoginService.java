package it.cnr.si.cool.jconon.agid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.si.cool.jconon.agid.repository.AGIDLoginRepository;
import it.cnr.si.cool.jconon.agid.repository.UserInfo;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Map;
import java.util.Optional;

@Service
public class AGIDLoginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDLoginService.class);

    private final CMISService cmisService;
    private final UserService userService;
    private final AGIDLoginRepository agidLoginRepository;

    public AGIDLoginService(CMISService cmisService, UserService userService, AGIDLoginRepository agidLoginRepository) {
        this.cmisService = cmisService;
        this.userService = userService;
        this.agidLoginRepository = agidLoginRepository;
    }

    public String createTicket(UserInfo userInfo) {
        CMISUser cmisUser = new CMISUser();
        cmisUser.setFirstName(userInfo.getFirstname());
        cmisUser.setLastName(userInfo.getFirstname());
        cmisUser.setCodicefiscale(userInfo.getFiscalNumber());
        cmisUser.setEmail(userInfo.getEmail());
        final Optional<CMISUser> userByCodiceFiscale =
                Optional.ofNullable(userService.findUserByCodiceFiscale(cmisUser.getCodicefiscale(), cmisService.getAdminSession()));
        if (userByCodiceFiscale.isPresent()) {
            return createTicketForUser(userByCodiceFiscale.get());
        } else {
            String userName = normalize(cmisUser.getFirstName())
                    .toLowerCase()
                    .concat(".")
                    .concat(normalize(cmisUser.getLastName())
                            .toLowerCase());
            //Verifico se l'utenza ha lo stesso codice fiscale
            try {
                Optional<CMISUser> cmisUser2 = Optional.ofNullable(userService.loadUserForConfirm(userName))
                        .filter(cmisUser1 -> cmisUser1.getCodicefiscale().equalsIgnoreCase(cmisUser.getCodicefiscale()));
                if (cmisUser2.isPresent()) {
                    return createTicketForUser(cmisUser2.get());
                }
            } catch (CoolUserFactoryException _ex) {
                LOGGER.trace("AGIG Login Username {} not found", userName);
            }
            if (!userService.isUserExists(userName)) {
                cmisUser.setUserName(userName);
            } else {
                for (int i = 1; i < 20; i++) {
                    final String concatUsername = userName.concat("0").concat(String.valueOf(i));
                    if (!userService.isUserExists(concatUsername)) {
                        cmisUser.setUserName(concatUsername);
                        break;
                    }
                }
            }
            final CMISUser user = userService.createUser(cmisUser);
            userService.enableAccount(user.getUserName());
            return createTicketForUser(user);
        }
    }

    private String normalize(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\W", "");
    }

    private String createTicketForUser(CMISUser cmisUser) {
        try {
            String link = cmisService.getBaseURL().concat("service/cnr/jconon/get-ticket/").concat(cmisUser.getId());
            UrlBuilder urlBuilder = new UrlBuilder(link);
            org.apache.chemistry.opencmis.client.bindings.spi.http.Response response =
                    CmisBindingsHelper.getHttpInvoker(cmisService.getAdminSession()).invokeGET(urlBuilder, cmisService.getAdminSession());
            ObjectMapper objectMapper = new ObjectMapper();
            if (response.getResponseCode() == HttpStatus.SC_OK) {
                @SuppressWarnings("unchecked")
                Map<String, String> readValue = objectMapper.readValue(response.getStream(), Map.class);
                return readValue.get("ticket");
            }
        } catch (IOException _ex) {
            LOGGER.error("Cannot create ticket for user {}", cmisUser.getId(), _ex);
        }
        return null;
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void evictAuthnRequest() {
        agidLoginRepository.removeAllState();
        LOGGER.info("AGID Login remove all State");
    }

}
