package it.cnr.si.cool.jconon.agid.controller;

import it.cnr.si.cool.jconon.agid.config.AGIDLoginConfigurationProperties;
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
    private final AGIDLoginConfigurationProperties agidLoginConfigurationProperties;

    public AGIDLoginController(AGIDLoginConfigurationProperties agidLoginConfigurationProperties) {
        this.agidLoginConfigurationProperties = agidLoginConfigurationProperties;
    }

    @GetMapping("/auth")
    public ModelAndView redirect(ModelMap model) {
        LOGGER.debug("Redirect to AGIDLogin");
        model.addAttribute("client_id", agidLoginConfigurationProperties.getClient_id());
        model.addAttribute("redirect_uri", agidLoginConfigurationProperties.getRedirect_uri());
        model.addAttribute("response_type", agidLoginConfigurationProperties.getResponse_type());
        model.addAttribute("scope", agidLoginConfigurationProperties.getScope());
        model.addAttribute("state", UUID.randomUUID().toString());
        return new ModelAndView("redirect:".concat(agidLoginConfigurationProperties.getAuth()), model);
    }

    @GetMapping("/response")
    public ModelAndView response(@RequestParam("code") String code, @RequestParam("state") String state) throws IOException, URISyntaxException {
        LOGGER.info("Code: {}", code);
        return new ModelAndView("redirect:/");
    }

}
