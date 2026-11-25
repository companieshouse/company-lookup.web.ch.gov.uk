package uk.gov.companieshouse.lookup.internationalisation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import uk.gov.companieshouse.session.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class ChSessionLocaleResolver implements LocaleResolver {
    // Read by common-web-java's chsBaseLayout: the language selector is shown
    // when welshLanguage.enabled is true or this request attribute is true.
    static final String ON_WELSH_JOURNEY_ATTRIBUTE = "onWelshJourney";

    private static final String LANG_SESSION_KEY = "lang";

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

    private SessionProvider sessionProvider;
    private final boolean welshLanguageEnabled;
    private Locale defaultLocale = Locale.getDefault();

    @Autowired
    public ChSessionLocaleResolver(SessionProvider sessionProvider, @Value("${welshLanguage.enabled:false}") boolean welshLanguageEnabled) {
        this.sessionProvider = sessionProvider;
        this.welshLanguageEnabled = welshLanguageEnabled;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        Locale locale = getLocaleFromSession().orElse(defaultLocale);
        httpServletRequest.setAttribute(ON_WELSH_JOURNEY_ATTRIBUTE, !defaultLocale.equals(locale));

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
        if (isWelshLanguageEnabled()) {
            setLocaleInSession(locale);
        }
    }

    // TEMPORARY (COM-169): the only place the Welsh rollout flag is checked.
    // While it is off the shared session is neither read nor written, so a
    // language preference set by another CHS service is left untouched.
    private boolean isWelshLanguageEnabled() {
        return welshLanguageEnabled;
    }

    private Optional<Locale> getLocaleFromSession() {
        if (!isWelshLanguageEnabled()) {
            return Optional.empty();
        }

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

    public void setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}
