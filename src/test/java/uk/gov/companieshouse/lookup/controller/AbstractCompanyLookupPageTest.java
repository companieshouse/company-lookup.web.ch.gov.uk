package uk.gov.companieshouse.lookup.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.companieshouse.lookup.config.MessageConfig;
import uk.gov.companieshouse.lookup.helper.LinkUtils;
import uk.gov.companieshouse.lookup.internationalisation.SessionProvider;
import uk.gov.companieshouse.lookup.service.CompanyLookupService;
import uk.gov.companieshouse.lookup.validation.ValidationHandler;
import uk.gov.companieshouse.session.Session;

/**
 * Renders the lookup page through the real locale resolver, interceptors, templates and message
 * bundles (company-lookup and common-web-java), with only the CHS session and the company profile
 * call mocked. CompanyLookupControllerTest mocks the resolver, so it cannot catch wiring faults
 * between the resolver, the language selector and the bundles; subclasses of this class can.
 */
@WebMvcTest(CompanyLookupController.class)
@Import({MessageConfig.class, ValidationHandler.class, LinkUtils.class})
abstract class AbstractCompanyLookupPageTest {

    static final String SEARCH_URL = "/company-lookup/search?forward={forward}";
    static final String FORWARD = "/efs-submission/1/company/{companyNumber}/details";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    SessionProvider sessionProvider;

    @MockitoBean
    CompanyLookupService companyLookupService;

    MockMvc mockMvc;
    Map<String, Object> sessionData;
    Session session;

    @BeforeAll
    static void setUp() {
        System.setProperty("COOKIE_NAME", "__SID");
    }

    // The shared CHS session is an in-memory map, so a language stored by one request is read
    // back by the same request's rendering, as it is in production.
    @BeforeEach
    void setUpSharedSession() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        sessionData = new HashMap<>();
        session = mock(Session.class);
        when(session.getData()).thenReturn(sessionData);
        when(sessionProvider.getSessionDataFromContext()).thenReturn(sessionData);
        when(sessionProvider.getSessionFromContext()).thenReturn(session);
    }

    Document render(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        return Jsoup.parse(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    static boolean hasLanguageSelector(Document page) {
        return page.selectFirst("nav.govuk-language-select") != null;
    }

    // The selector renders the current language as plain text and the other as a link.
    static String currentLanguage(Document page) {
        return page.select("nav.govuk-language-select span[aria-current=true]").text();
    }
}
