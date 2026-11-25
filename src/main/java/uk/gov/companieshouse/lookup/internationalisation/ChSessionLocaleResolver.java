package uk.gov.companieshouse.lookup.internationalisation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import uk.gov.companieshouse.session.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves the locale from the language preference held in the shared CHS
 * session (top-level "lang" key), so a choice made in any CHS service is
 * honoured here and vice versa.
 *
 * <p>Built by MessageConfig rather than component scanning: the class has no
 * Spring wiring of its own, so it can move to common-web-java unchanged.
 */
public class ChSessionLocaleResolver implements LocaleResolver {
    // Read by common-web-java's chsBaseLayout: the language selector is shown
    // when welshLanguage.enabled is true or this request attribute is true.
    // It is true when the session already holds a language, i.e. the user
    // arrived from a journey that offers a choice of language. That keeps the
    // selector off journeys that are English only while the rollout flag
    // (welshLanguage.enabled, COM-169) is off.
    static final String ON_WELSH_JOURNEY_ATTRIBUTE = "onWelshJourney";

    private static final String LANG_SESSION_KEY = "lang";

    // The "lang" session key is shared with other CHS services, so only
    // languages this service can render are written to it (?lang=fr is ignored).
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "cy");

    // TEMPORARY (IDVA6-2788): some CHS services (e.g. the Node.js sibling
    // company-lookup.web.ch.gov.uk and others listed in the ticket) still
    // write the user's language preference to the legacy nested
    // "extra_data.lang" session key instead of the shared top-level "lang"
    // key. Until every service is migrated to the shared key, we fall back
    // to reading the legacy location so a preference set by an unmigrated
    // service is not silently lost here. This resolver never writes to the
    // legacy location. Remove this fallback (and EXTRA_DATA_SESSION_KEY /
    // languageTagFromLegacyExtraData) once all CHS services write only the
    // top-level "lang" key.
    private static final String EXTRA_DATA_SESSION_KEY = "extra_data";

    private final SessionProvider sessionProvider;
    private final Locale defaultLocale;

    public ChSessionLocaleResolver(SessionProvider sessionProvider, Locale defaultLocale) {
        this.sessionProvider = Objects.requireNonNull(sessionProvider, "sessionProvider");
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale");
    }

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        Optional<Locale> sessionLocale = getLocaleFromSession();
        httpServletRequest.setAttribute(ON_WELSH_JOURNEY_ATTRIBUTE, sessionLocale.isPresent());

        return sessionLocale.orElse(defaultLocale);
    }

    // A null locale (?lang= with no value) is ignored rather than clearing a
    // preference that other CHS services also rely on.
    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
        if (isSupported(locale)) {
            setLocaleInSession(locale);
        }
    }

    private static boolean isSupported(Locale locale) {
        return locale != null && SUPPORTED_LANGUAGES.contains(locale.getLanguage());
    }

    private Optional<Locale> getLocaleFromSession() {
        Map<String, Object> sessionData = sessionProvider.getSessionDataFromContext();
        String languageTag = languageTagFrom(sessionData.get(LANG_SESSION_KEY));

        if (StringUtils.isEmpty(languageTag)) {
            // TEMPORARY (IDVA6-2788): fall back to the legacy nested
            // extra_data.lang location - see class-level comment.
            languageTag = languageTagFromLegacyExtraData(sessionData);
        }

        return StringUtils.isEmpty(languageTag)
                ? Optional.empty()
                : Optional.of(Locale.forLanguageTag(languageTag));
    }

    private String languageTagFrom(Object languageTagObj) {
        return languageTagObj instanceof String string ? string : null;
    }

    private String languageTagFromLegacyExtraData(Map<String, Object> sessionData) {
        Object extraDataObj = sessionData.get(EXTRA_DATA_SESSION_KEY);
        if (!(extraDataObj instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> extraData = (Map<String, String>) extraDataObj;
        return extraData.get(LANG_SESSION_KEY);
    }

    private void setLocaleInSession(Locale locale) {
        Session session = sessionProvider.getSessionFromContext();
        session.getData().put(LANG_SESSION_KEY, locale.toLanguageTag());
        session.store();
    }
}
