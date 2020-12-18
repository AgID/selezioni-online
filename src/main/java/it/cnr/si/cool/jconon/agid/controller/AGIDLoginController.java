package it.cnr.si.cool.jconon.agid.controller;

import it.cnr.si.cool.jconon.agid.config.AGIDLoginConfigurationProperties;
import it.cnr.si.cool.jconon.agid.repository.AGIDLogin;
import it.cnr.si.cool.jconon.agid.repository.AccessToken;
import it.cnr.si.cool.jconon.agid.repository.UserInfo;
import it.cnr.si.cool.jconon.agid.service.AGIDLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.AuthenticationException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@RestController
@RequestMapping("/openapi/agid-login")
@EnableConfigurationProperties(AGIDLoginConfigurationProperties.class)
public class AGIDLoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDLoginController.class);
    @Value("${cookie.secure}")
    private Boolean cookieSecure;
    private final AGIDLoginConfigurationProperties properties;
    private final AGIDLogin agidLogin;
    private final AGIDLoginService agidLoginService;

    public AGIDLoginController(AGIDLoginConfigurationProperties properties, AGIDLogin agidLogin, AGIDLoginService agidLoginService) {
        this.properties = properties;
        this.agidLogin = agidLogin;
        this.agidLoginService = agidLoginService;
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
    public ModelAndView response(ModelMap model,
                                 HttpServletResponse res,
                                 HttpServletRequest req,
                                 @RequestParam("code") String code,
                                 @RequestParam("state") String state
    ) throws IOException, URISyntaxException {
        LOGGER.info("Code: {}", code);
        AccessToken accessToken = agidLogin.getTokenFull(
                "authorization_code",
                code,
                properties.getRedirect_uri(),
                properties.getClient_id(),
                properties.getClient_secret());
        LOGGER.info("AccessToken: {}", accessToken);
        UserInfo userInfo = agidLogin.getUserInfo(
                "Bearer ".concat(accessToken.getAccess_token()),
                properties.getClient_id(),
                properties.getClient_secret());
        LOGGER.info("UserInfo: {}", userInfo);
        try {
            final String ticket = agidLoginService.createTicket(userInfo);
            res.addCookie(getCookie(ticket, req.isSecure()));
            return new ModelAndView("redirect:/");
        } catch (Exception e) {
            LOGGER.warn("Cannot create ticket from AGIG Login ", e);
            model.addAttribute("failureMessage", e.getMessage());
            return new ModelAndView("redirect:/login", model);
        }
    }

    private Cookie getCookie(String ticket, boolean secure) {
        int maxAge = ticket == null ? 0 : 3600;
        Cookie cookie = new Cookie("ticket", ticket);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure && cookieSecure);
        cookie.setHttpOnly(true);
        return cookie;
    }

}
