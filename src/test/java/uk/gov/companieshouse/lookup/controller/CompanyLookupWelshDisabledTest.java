package uk.gov.companieshouse.lookup.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * The Welsh rollout flag is off (welshLanguage.enabled=false), as it is in live until COM-169.
 * Journeys that already offer Welsh must keep working; English-only journeys must not change.
 */
@TestPropertySource(locations = "classpath:application-test.properties",
        properties = "welshLanguage.enabled=false")
class CompanyLookupWelshDisabledTest extends AbstractCompanyLookupPageTest {

    @Test
    @DisplayName("English-only journey (no language in the session): English, no selector")
    void englishOnlyJourneyHasNoSelector() throws Exception {
        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(hasLanguageSelector(page)).isFalse();
    }

    @Test
    @DisplayName("Welsh journey (session language cy): Welsh, with the selector")
    void welshJourneyStaysWelsh() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("h1").text()).isEqualTo("Beth yw rhif y cwmni?");
        assertThat(currentLanguage(page)).isEqualTo("Cymraeg");
    }

    @Test
    @DisplayName("Welsh journey: validation errors are Welsh")
    void welshJourneyValidationErrorIsWelsh() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(post(SEARCH_URL, FORWARD).param("companyNumber", "12"));

        assertThat(page.selectFirst(".govuk-error-summary__title").text()).isEqualTo("Mae yna broblem");
        assertThat(hasLanguageSelector(page)).isTrue();
    }

    @Test
    @DisplayName("Language-aware journey in English (session language en): can switch to Welsh")
    void languageAwareJourneyCanSwitchToWelsh() throws Exception {
        sessionData.put("lang", "en");

        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(page.select("nav.govuk-language-select a").attr("href")).endsWith("lang=cy");
    }
}
