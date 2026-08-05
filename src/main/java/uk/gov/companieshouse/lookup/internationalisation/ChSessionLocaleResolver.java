package uk.gov.companieshouse.lookup.internationalisation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import uk.gov.companieshouse.session.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

@Component
public class ChSessionLocaleResolver implements LocaleResolver {
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
    private Locale defaultLocale = Locale.getDefault();

    @Autowired
    public ChSessionLocaleResolver(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        Locale locale = getLocaleFromSession();
        httpServletRequest.setAttribute("onWelshJourney", locale != null);
        if (locale == null) {
            locale = defaultLocale;
        }

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
        setLocaleInSession(locale);
    }

    private Locale getLocaleFromSession() {
        Map<String, Object> sessionData = sessionProvider.getSessionDataFromContext();
        String languageTag = languageTagFrom(sessionData.get(LANG_SESSION_KEY));

        if (StringUtils.isEmpty(languageTag)) {
            // TEMPORARY (IDVA6-2788): fall back to the legacy nested
            // extra_data.lang location - see class-level comment.
            languageTag = languageTagFromLegacyExtraData(sessionData);
        }

        if (StringUtils.isEmpty(languageTag)) {
            return null;
        }

        return Locale.forLanguageTag(languageTag);
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
