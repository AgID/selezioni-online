package it.cnr.si.cool.jconon.agid.controller;

import it.cnr.si.cool.jconon.agid.config.AGIDLoginConfigurationProperties;
import it.cnr.si.cool.jconon.agid.repository.AGIDLogin;
import it.cnr.si.cool.jconon.agid.repository.AccessToken;
import it.cnr.si.cool.jconon.agid.repository.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@RestController
@RequestMapping("/openapi/agid-login")
@EnableConfigurationProperties(AGIDLoginConfigurationProperties.class)
public class AGIDLoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDLoginController.class);
    private final AGIDLoginConfigurationProperties properties;
    private final AGIDLogin agidLogin;

    public AGIDLoginController(AGIDLoginConfigurationProperties properties, AGIDLogin agidLogin) {
        this.properties = properties;
        this.agidLogin = agidLogin;
    }

    @GetMapping("/auth")
    public ModelAndView redirect(ModelMap model) {
        model.addAttribute("client_id", properties.getClient_id());
        model.addAttribute("redirect_uri", properties.getRedirect_uri());
        model.addAttribute("response_type", properties.getResponse_type());
        model.addAttribute("scope", properties.getScope());
        model.addAttribute("state", UUID.randomUUID().toString());
        return new ModelAndView("redirect:".concat(properties.getAuth()), model);
    }

    @GetMapping("/response")
    public ModelAndView response(@RequestParam("code") String code, @RequestParam("state") String state) throws IOException, URISyntaxException {
        LOGGER.info("Code: {}", code);
        AccessToken accessToken = agidLogin.getTokenFull("authorization_code", code, properties.getRedirect_uri());
        LOGGER.info("AccessToken: {}", accessToken);
        UserInfo userInfo = agidLogin.getUserInfo(accessToken.getAccess_token());
        LOGGER.info("UserInfo: {}", userInfo);
        return new ModelAndView("redirect:/");
    }

}
