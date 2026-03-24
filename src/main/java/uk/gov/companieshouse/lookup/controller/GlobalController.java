package uk.gov.companieshouse.lookup.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.LocaleResolver;

@ControllerAdvice
public class GlobalController {

    private final LocaleResolver localeResolver;

    public GlobalController(final LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @ModelAttribute("requestURI")
    String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("locale")
    String getLocale(HttpServletRequest request) {
        return localeResolver.resolveLocale(request).toString();
    }

    @ModelAttribute("request")
    HttpServletRequest getRequest(HttpServletRequest request) {
        return request;
    }
}
