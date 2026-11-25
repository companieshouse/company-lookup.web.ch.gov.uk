package uk.gov.companieshouse.lookup.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.stream.Stream;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.companieshouse.lookup.model.Company;

/**
 * Welsh enabled for every journey (welshLanguage.enabled=true). The lookup page is shared by all
 * calling journeys, so these cases cover the Welsh acceptance criteria of COM-165, COM-167 and
 * COM-168 together.
 */
@TestPropertySource(locations = "classpath:application-test.properties",
        properties = "welshLanguage.enabled=true")
class CompanyLookupWelshJourneyTest extends AbstractCompanyLookupPageTest {

    private static final String COMPANY_NUMBER = "00000587";

    @Test
    @DisplayName("No language chosen: page is English and offers the language selector")
    void englishByDefaultWithLanguageSelector() throws Exception {
        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(page.selectFirst("html").attr("lang")).isEqualTo("en");
        assertThat(currentLanguage(page)).isEqualTo("English");
    }

    @Test
    @DisplayName("Cymraeg link: page is Welsh and the choice is stored in the shared session")
    void cymraegLinkSwitchesToWelsh() throws Exception {
        Document page = render(get(SEARCH_URL, FORWARD).param("lang", "cy"));

        assertThat(page.selectFirst("h1").text()).isEqualTo("Beth yw rhif y cwmni?");
        assertThat(page.getElementById("company-lookup-hint").text()).isEqualTo("Rhowch rif cwmni sy'n 8 nod.");
        assertThat(page.getElementById("next-button").attr("value")).isEqualTo("Parhau");
        assertThat(currentLanguage(page)).isEqualTo("Cymraeg");
        assertThat(sessionData).containsEntry("lang", "cy");
        verify(session).store();
    }

    @Test
    @DisplayName("English link: a Welsh session reverts to English and can switch back")
    void englishLinkRevertsToEnglish() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(get(SEARCH_URL, FORWARD).param("lang", "en"));

        assertThat(page.selectFirst("h1").text()).isEqualTo("What is the company number?");
        assertThat(currentLanguage(page)).isEqualTo("English");
        assertThat(page.select("nav.govuk-language-select a").attr("href")).endsWith("lang=cy");
        assertThat(sessionData).containsEntry("lang", "en");
    }

    static Stream<Arguments> welshErrors() {
        return Stream.of(
                Arguments.of("no company number", "", false,
                        "Rhowch rif y cwmni"),
                Arguments.of("too few characters", "1234567", false,
                        "Rhaid i rif y cwmni fod ag 8 nod. Os yw'n 7 nod neu lai, nodwch seroau ar y "
                                + "dechrau fel ei fod yn 8 nod i gyd."),
                Arguments.of("special characters", "1234567!", false,
                        "Rhaid i rif cwmni gynnwys rhifau a llythrennau A i Z yn unig"),
                Arguments.of("company not found", "99999999", false,
                        "Ni allwn ddod o hyd i'r rhif cwmni hwn. Gwiriwch yr hyn rydych wedi'i nodi a "
                                + "rhowch gynnig arall arni."),
                Arguments.of("inactive company with activeOnly", COMPANY_NUMBER, true,
                        "Rhowch rif cwmni ar gyfer cwmni sy'n weithredol"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("welshErrors")
    @DisplayName("Welsh error messages")
    void welshErrorMessages(String scenario, String companyNumber, boolean activeOnly, String expected)
            throws Exception {
        sessionData.put("lang", "cy");
        when(companyLookupService.getCompanyProfile(COMPANY_NUMBER)).thenReturn(company("dissolved"));

        Document page = render(post(SEARCH_URL + (activeOnly ? "&activeOnly=true" : ""), FORWARD)
                .param("companyNumber", companyNumber));

        assertThat(page.selectFirst(".govuk-error-summary__title").text()).isEqualTo("Mae yna broblem");
        assertThat(page.getElementById("error-text").text()).isEqualTo(expected);
        assertThat(page.getElementById("company-lookup-error").text()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Without activeOnly an inactive company continues to the calling journey")
    void inactiveCompanyContinuesWithoutActiveOnly() throws Exception {
        sessionData.put("lang", "cy");
        when(companyLookupService.getCompanyProfile(COMPANY_NUMBER)).thenReturn(company("dissolved"));

        MvcResult result = mockMvc.perform(post(SEARCH_URL, FORWARD).param("companyNumber", COMPANY_NUMBER))
                .andReturn();

        assertThat(result.getResponse().getRedirectedUrl())
                .isEqualTo("/efs-submission/1/company/" + COMPANY_NUMBER + "/details");
    }

    @Test
    @DisplayName("Screen-reader text and the page language are Welsh")
    void screenReaderElementsAreWelsh() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.selectFirst("html").attr("lang")).isEqualTo("cy");
        assertThat(page.selectFirst("a.govuk-skip-link").text()).isEqualTo("Neidio i'r prif gynnwys");
        assertThat(page.selectFirst("nav.govuk-language-select").attr("aria-label")).isEqualTo("Newidydd iaith");
        assertThat(page.selectFirst("footer h2.govuk-visually-hidden").text()).isEqualTo("Dolenni cymorth");
        assertThat(page.getElementById("cookie-banner").attr("aria-label"))
                .isEqualTo("Cwcis ar wasanaethau Tŷ'r Cwmnïau");
    }

    @Test
    @DisplayName("Cookie banner choices and confirmations are Welsh")
    void cookieBannerIsWelsh() throws Exception {
        sessionData.put("lang", "cy");

        Document page = render(get(SEARCH_URL, FORWARD));

        assertThat(page.getElementById("accept-cookies-button").text()).isEqualTo("Derbyn cwcis dadansoddol");
        assertThat(page.getElementById("reject-cookies-button").text()).isEqualTo("Gwrthod cwcis dadansoddol");
        assertThat(page.select("#accepted-cookies-message p").text())
                .startsWith("Rydych chi wedi derbyn cwcis dadansoddol. Gallwch newid eich gosodiadau cwcis");
        assertThat(page.select("#rejected-cookies-message p").text())
                .startsWith("Rydych chi wedi gwrthod cwcis dadansoddol. Gallwch newid eich gosodiadau cwcis");
        assertThat(page.getElementById("hide-accepted-message-button").text()).isEqualTo("Cuddio'r neges hon");
    }

    private static Company company(String status) {
        Company company = new Company();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setStatus(status);
        return company;
    }
}
