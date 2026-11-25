package uk.gov.companieshouse.lookup.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import uk.gov.companieshouse.lookup.internationalisation.ChSessionLocaleResolver;
import uk.gov.companieshouse.lookup.internationalisation.SessionProvider;

@Configuration
public class MessageConfig implements WebMvcConfigurer {
    @Bean("messageSource")
    public MessageSource messageSource() {
        var messageSource = new ResourceBundleMessageSource();

        // This will match the resources folder files that begin with 'messages'
        // e.g. messages_wl.properties, messages_fr.properties
        messageSource.setBasenames("locales/messages", "locales/common-messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    // The DispatcherServlet's LocaleResolver: reads and writes the language in
    // the shared CHS session. The return type is the concrete class so tests
    // can replace this bean with @MockitoBean ChSessionLocaleResolver.
    @Bean
    public ChSessionLocaleResolver localeResolver(SessionProvider sessionProvider) {
        return new ChSessionLocaleResolver(sessionProvider, Locale.ENGLISH);
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        // Ignore malformed values such as ?lang=<script> instead of returning a 500 error page.
        localeChangeInterceptor.setIgnoreInvalidLocale(true);
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
