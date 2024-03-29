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

package it.cnr.si.cool.jconon.agid.controller;

import it.cnr.cool.rest.SecurityRest;
import it.cnr.si.cool.jconon.agid.config.AGIDLoginConfigurationProperties;
import it.cnr.si.cool.jconon.agid.repository.AGIDLogin;
import it.cnr.si.cool.jconon.agid.repository.AGIDLoginRepository;
import it.cnr.si.cool.jconon.agid.repository.AccessToken;
import it.cnr.si.cool.jconon.agid.repository.UserInfo;
import it.cnr.si.cool.jconon.agid.service.AGIDLoginService;
import it.cnr.si.cool.jconon.util.CodiceFiscaleControllo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/openapi/agid-login")
@EnableConfigurationProperties(AGIDLoginConfigurationProperties.class)
public class AGIDLoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AGIDLoginController.class);
    public static final String AGID_LOGIN_TOKEN = "AGIDLoginToken";
    public static final String SPID = "SPID";
    private final AGIDLoginConfigurationProperties properties;
    private final AGIDLogin agidLogin;
    private final AGIDLoginService agidLoginService;
    private final AGIDLoginRepository agidLoginRepository;
    @Value("${cookie.secure}")
    private Boolean cookieSecure;

    @Autowired
    SecurityRest securityRest;

    public AGIDLoginController(
            AGIDLoginConfigurationProperties properties,
            AGIDLogin agidLogin,
            AGIDLoginService agidLoginService,
            AGIDLoginRepository agidLoginRepository
    ) {
        this.properties = properties;
        this.agidLogin = agidLogin;
        this.agidLoginService = agidLoginService;
        this.agidLoginRepository = agidLoginRepository;
    }

    @GetMapping("/auth")
    public ModelAndView redirect(ModelMap model) {
        model.addAttribute("client_id", properties.getClient_id());
        model.addAttribute("redirect_uri", properties.getRedirect_uri());
        model.addAttribute("response_type", properties.getResponse_type());
        model.addAttribute("scope", properties.getScope());
        model.addAttribute("state", agidLoginRepository.register());
        return new ModelAndView("redirect:".concat(properties.getAuth()), model);
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest req, HttpServletResponse res, ModelMap model) {
        securityRest.logout(req, res);
        res.addCookie(getCookieAgiDLogin(null, req.isSecure()));
        return new ModelAndView("redirect:".concat("/"));
    }

    @GetMapping("/internal-logout")
    public ModelAndView internalLogout(HttpServletRequest req, HttpServletResponse res, ModelMap model) {
        final Optional<String> agidLoginToken = Arrays.asList(
                    Optional.ofNullable(req.getCookies()).orElse(new Cookie[0])
                )
                .stream()
                .filter(cookie -> cookie.getName().equalsIgnoreCase(AGID_LOGIN_TOKEN))
                .map(cookie -> cookie.getValue())
                .findAny();
        if (agidLoginToken.isPresent()) {
            model.addAttribute("post_logout_redirect_uri", properties.getPost_logout_redirect_uri());
            model.addAttribute("state", agidLoginRepository.register());
            model.addAttribute("id_token_hint", agidLoginToken.get());
            return new ModelAndView("redirect:".concat(properties.getLogout()), model);
        } else {
            securityRest.logout(req, res);
        }
        return new ModelAndView("redirect:".concat("/"));
    }

    @GetMapping("/response")
    public ModelAndView response(ModelMap model,
                                 HttpServletResponse res,
                                 HttpServletRequest req,
                                 @RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "state", required = false) String state,
                                 @RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "error_description", required = false) String error_description
    ) throws IOException, URISyntaxException {
        if (!Optional.ofNullable(error).isPresent() && agidLoginRepository.isStateValid(state)) {
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
            if (Optional.ofNullable(userInfo)
                    .flatMap(userInfo1 -> Optional.ofNullable(userInfo1.getSub()))
                    .filter(sub -> !sub.startsWith(SPID))
                    .isPresent() &&
                        !Optional.ofNullable(userInfo)
                            .flatMap(userInfo1 -> Optional.ofNullable(userInfo1.getFiscalNumber()))
                            .isPresent()) {
                if (Optional.ofNullable(userInfo)
                        .flatMap(userInfo1 -> Optional.ofNullable(userInfo1.getSub()))
                        .filter(sub -> sub.startsWith("CIE"))
                        .isPresent()) {
                   userInfo.setFiscalNumber(userInfo.getSub().replace("CIE:TINIT-",""));
                } else {
                    userInfo.setFiscalNumber(calculateFiscalNumber(userInfo));
                }
            }
            try {
                if (!Optional.ofNullable(userInfo.getFirstname()).isPresent() ||
                        !Optional.ofNullable(userInfo.getLastname()).isPresent() ||
                        !Optional.ofNullable(userInfo.getFiscalNumber()).isPresent()) {
                    model.addAttribute("failureMessage", "agid-userinfo-notcomplete");
                    return new ModelAndView("redirect:/", model);
                }
                final String ticket = agidLoginService.createTicket(userInfo);
                res.addCookie(getCookie(ticket, req.isSecure()));
                res.addCookie(getCookieAgiDLogin(accessToken.getId_token(), req.isSecure()));
                return new ModelAndView("redirect:/");
            } catch (Exception e) {
                LOGGER.warn("Cannot create ticket from AGIG Login ", e);
                model.addAttribute("failureMessage", e.getMessage());
                return new ModelAndView("redirect:/", model);
            }
        } else {
            model.addAttribute(
                    "failureMessage",
                    Optional.ofNullable(error).map(s -> "agid-login-".concat(s)).orElse("agid-state-notfound")
            );
            return new ModelAndView("redirect:/", model);
        }
    }

    private String calculateFiscalNumber(UserInfo userInfo) {
        return Arrays.asList(
            CodiceFiscaleControllo.calcolaCodCognome(
                    Optional.ofNullable(userInfo)
                        .flatMap(userInfo1 -> Optional.ofNullable(userInfo1.getLastname()))
                        .map(String::toUpperCase)
                        .orElse("XXX")
            ),
            CodiceFiscaleControllo.calcolaCodNome(
                    Optional.ofNullable(userInfo)
                            .flatMap(userInfo1 -> Optional.ofNullable(userInfo1.getFirstname()))
                            .map(String::toUpperCase)
                            .orElse("XXX")
            ),
            "00A00A000A"
        ).stream().collect(Collectors.joining());
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

    private Cookie getCookieAgiDLogin(String id_token, boolean secure) {
        int maxAge = id_token == null ? 0 : 3600;
        Cookie cookie = new Cookie(AGID_LOGIN_TOKEN, id_token);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure && cookieSecure);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
