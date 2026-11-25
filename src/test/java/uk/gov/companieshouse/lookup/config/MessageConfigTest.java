package uk.gov.companieshouse.lookup.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

class MessageConfigTest {

    @Test
    @DisplayName("Language changes use ?lang and ignore malformed values instead of erroring")
    void localeChangeInterceptorIgnoresInvalidLocales() {
        LocaleChangeInterceptor interceptor = new MessageConfig().localeChangeInterceptor();

        assertEquals("lang", interceptor.getParamName());
        assertTrue(interceptor.isIgnoreInvalidLocale());
    }
}
