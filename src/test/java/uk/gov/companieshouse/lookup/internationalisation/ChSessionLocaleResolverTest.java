package uk.gov.companieshouse.lookup.internationalisation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.session.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ChSessionLocaleResolverTest {

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private Session session;

    @Mock
    private SessionProvider sessionProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Map<String, Object> sessionData;

    private ChSessionLocaleResolver localeResolver;


    @BeforeEach
    public void setUp() {
        localeResolver = new ChSessionLocaleResolver(sessionProvider, Locale.ENGLISH);
    }

    // Language tag is now stored directly under the top-level shared session
    // key "lang", not nested under "extra_data", so that it is shared
    // correctly with other CHS services (e.g. authentication-service,
    // your-companies-web) that use the same top-level key.
    private void initSessionData(String lang) {
        sessionData = new HashMap<>();
        if (lang != null) {
            sessionData.put("lang", lang);
        }
    }

    private void setupSessionData(String lang) {
        initSessionData(lang);
        when(sessionProvider.getSessionDataFromContext()).thenReturn(sessionData);
    }

    // TEMPORARY (IDVA6-2788): some CHS services may still be writing the
    // legacy nested "extra_data.lang" key instead of the shared top-level
    // "lang" key. This sets up that legacy nested value only, with no
    // top-level "lang" key present, to verify the fallback read path.
    private void setupLegacyExtraDataSessionData(String lang) {
        sessionData = new HashMap<>();
        Map<String, String> extraData = new HashMap<>();
        if (lang != null) {
            extraData.put("lang", lang);
        }
        sessionData.put("extra_data", extraData);
        when(sessionProvider.getSessionDataFromContext()).thenReturn(sessionData);
    }

    private void setupSession() {
        initSessionData(null);
        when(sessionProvider.getSessionFromContext()).thenReturn(session);
        when(session.getData()).thenReturn(sessionData);
    }

    @Test
    @DisplayName("Resolves to the default locale if no locale is in the session")
    void testResolveDefaultLocale() {
        setupSessionData(null);
        Locale locale = localeResolver.resolveLocale(request);

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    @DisplayName("Resolves to the locale in the session when present")
    void testResolveLocale() {
        setupSessionData("cy");
        Locale locale = localeResolver.resolveLocale(request);

        Locale welshLocale = Locale.forLanguageTag("cy");
        assertEquals(welshLocale, locale);
    }

    @Test
    @DisplayName("Locale gets set in session when resolver sets locale")
    void testSetLocale() {
        setupSession();
        Locale welshLocale = Locale.forLanguageTag("cy");

        localeResolver.setLocale(request, response, welshLocale);

        String languageTagInSession = (String) session.getData().get("lang");

        assertEquals(welshLocale.toLanguageTag(), languageTagInSession);
        verify(session).store();
    }

    @Test
    @DisplayName("Set default locale and resolves to the default locale if no locale is in the session")
    void testSetDefaultLocaleAndResolveDefaultLocale() {
        setupSessionData(null);
        localeResolver = new ChSessionLocaleResolver(sessionProvider, Locale.US);
        Locale locale = localeResolver.resolveLocale(request);

        assertEquals(Locale.US, locale);
    }

    @Test
    @DisplayName("TEMPORARY (IDVA6-2788): falls back to the legacy extra_data.lang "
            + "value when the top-level lang key is absent")
    void testResolveLocaleFallsBackToLegacyExtraDataLang() {
        setupLegacyExtraDataSessionData("cy");
        Locale locale = localeResolver.resolveLocale(request);

        Locale welshLocale = Locale.forLanguageTag("cy");
        assertEquals(welshLocale, locale);
    }

    @Test
    @DisplayName("TEMPORARY (IDVA6-2788): top-level lang key takes precedence "
            + "over the legacy extra_data.lang value when both are present")
    void testResolveLocalePrefersTopLevelLangOverLegacyExtraDataLang() {
        initSessionData("cy");
        Map<String, String> extraData = new HashMap<>();
        extraData.put("lang", "en");
        sessionData.put("extra_data", extraData);
        when(sessionProvider.getSessionDataFromContext()).thenReturn(sessionData);

        Locale locale = localeResolver.resolveLocale(request);

        Locale welshLocale = Locale.forLanguageTag("cy");
        assertEquals(welshLocale, locale);
    }

    @ParameterizedTest(name = "session lang [{0}] sets onWelshJourney to {1}")
    @CsvSource(value = {"cy, true", "en, true", "NONE, false"}, nullValues = "NONE")
    @DisplayName("onWelshJourney is set when the session already holds a language")
    void testOnWelshJourneyWhenWelshEnabled(String sessionLang, boolean expectedOnWelshJourney) {
        setupSessionData(sessionLang);

        localeResolver.resolveLocale(request);

        verify(request).setAttribute("onWelshJourney", expectedOnWelshJourney);
    }

    @ParameterizedTest(name = "[{0}]")
    @NullSource
    @ValueSource(strings = {"fr", "und"})
    @DisplayName("Missing or unsupported languages are not written to the shared session")
    void testUnsupportedLocaleIsNotStored(String languageTag) {
        Locale locale = languageTag == null ? null : Locale.forLanguageTag(languageTag);

        localeResolver.setLocale(request, response, locale);

        verifyNoInteractions(sessionProvider);
    }
}