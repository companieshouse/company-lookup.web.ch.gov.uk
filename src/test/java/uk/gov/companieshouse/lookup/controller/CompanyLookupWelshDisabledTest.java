package uk.gov.companieshouse.lookup.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** The Welsh rollout flag is off (welshLanguage.enabled=false), as it is in live until COM-169. */
@TestPropertySource(locations = "classpath:application-test.properties",
        properties = "welshLanguage.enabled=false")
class CompanyLookupWelshDisabledTest extends AbstractCompanyLookupPageTest {

    @Test
    @DisplayName("A Welsh preference in the shared session is ignored and left untouched")
    void welshSessionRendersEnglishWithoutSelector() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(hasLanguageSelector(page)).isFalse();
        verifyNoInteractions(sessionProvider);
    }

    @Test
    @DisplayName("?lang=cy is ignored")
    void langParameterIsIgnored() throws Exception {
        Document page = render(get(SEARCH_URL, FORWARD).param("lang", "cy"));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(hasLanguageSelector(page)).isFalse();
        assertThat(sessionData).isEmpty();
    }

    @Test
    @DisplayName("Validation errors are English and the selector stays hidden")
    void validationErrorIsEnglishWithoutSelector() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(post(SEARCH_URL, FORWARD).param("companyNumber", "12"));

        assertThat(page.selectFirst(".govuk-error-summary__title").text()).isEqualTo("There is a problem");
        assertThat(hasLanguageSelector(page)).isFalse();
    }
}
