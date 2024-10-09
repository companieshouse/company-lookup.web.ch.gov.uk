package uk.gov.companieshouse.lookup.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.lookup.exception.RequestExceptionHandler;
import uk.gov.companieshouse.lookup.model.Company;
import uk.gov.companieshouse.lookup.model.CompanyLookup;
import uk.gov.companieshouse.lookup.service.CompanyLookupService;
import uk.gov.companieshouse.lookup.validation.ValidationHandler;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyLookupControllerTest {

    private static final String COMPANY_LOOKUP_URL = "/company-lookup/search?forward={forward}";
    private static final String COMPANY_LOOKUP_NO_NUMBER_URL = "/company-lookup/no-number?forward={forward}";
    private static final String TEST_PATH = "companyLookup.companyNumber";
    private static final String TEMPLATE = "lookup/companyLookup";
    private static final String ERROR_TEMPLATE = "error";
    private static final String MODEL_ATTRIBUTE = "companyLookup";
    private static final String FORWARD_URL_PARAM = "forwardURL";
    private static final String COMPANY_NUMBER = "12345678";

    private MockMvc mockMvc;

    @Mock
    private CompanyLookupService companyLookupService;

    @Mock
    private ValidationHandler validationHandler;

    @Mock
    private Company company;

    @Mock
    private CompanyLookup companyLookup;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ApiErrorResponseException apiErrorResponseException;

    @InjectMocks
    private CompanyLookupController companyLookupController;

    @BeforeEach
    public void setUpBeforeEAch() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(companyLookupController)
                .setControllerAdvice(new RequestExceptionHandler()).build();
    }

    @Test
    @DisplayName("Get Company Lookup - Success")
    void getCompanyLookup() throws Exception {
        this.mockMvc.perform(get(COMPANY_LOOKUP_URL, FORWARD_URL_PARAM))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(view().name(TEMPLATE))
                .andExpect(model().attributeExists(MODEL_ATTRIBUTE)).andReturn();
    }

    @Test
    @DisplayName("Get Company Lookup - Failed, bad forward URL")
    void getCompanyLookupWhenForwardUrlBad() throws Exception {
        this.mockMvc.perform(get(COMPANY_LOOKUP_URL, "@:bad-forward-url"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(view().name(ERROR_TEMPLATE))
                .andExpect(model().attributeDoesNotExist(MODEL_ATTRIBUTE))
                .andReturn();
    }

    @Test
    @DisplayName("Get Company Lookup Without Number - Success")
    void getCompanyLookupWithoutNumber() throws Exception {
        this.mockMvc.perform(get(COMPANY_LOOKUP_NO_NUMBER_URL, FORWARD_URL_PARAM))
                .andDo(print()).andExpect(status().is3xxRedirection())
                .andExpect(
                        view().name(UrlBasedViewResolver.REDIRECT_URL_PREFIX + FORWARD_URL_PARAM));
    }

    @Test
    @DisplayName("Get Company Lookup Without Number - Failed, bad forward URL")
    void getCompanyLookupWithoutNumberWhenForwardUrlBad() throws Exception {
        this.mockMvc.perform(get(COMPANY_LOOKUP_NO_NUMBER_URL, "@:bad-forward-url"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(view().name(ERROR_TEMPLATE))
                .andExpect(model().attributeDoesNotExist(MODEL_ATTRIBUTE))
                .andReturn();
    }

    @Test
    @DisplayName("Post Company Lookup - Success")
    void postCompanyLookup() throws Exception {
        when(companyLookupService.getCompanyProfile(COMPANY_NUMBER)).thenReturn(company);
        this.mockMvc
                .perform(post(COMPANY_LOOKUP_URL, FORWARD_URL_PARAM).param("companyNumber",
                        COMPANY_NUMBER))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        view().name(UrlBasedViewResolver.REDIRECT_URL_PREFIX + FORWARD_URL_PARAM));
    }

    @Test
    @DisplayName("Post Company Lookup - Fail bind error")
    void postCompanyLookupBindFail() throws Exception {
        this.mockMvc.perform(post(COMPANY_LOOKUP_URL, FORWARD_URL_PARAM)
                        .param(TEST_PATH, "test"))
                .andExpect(status().isOk())
                .andExpect(view().name(TEMPLATE))
                .andExpect(model().attributeExists(MODEL_ATTRIBUTE));
    }

    @Test
    @DisplayName("Post Company Lookup - Failed to find the company")
    void postCompanyLookupFail() throws Exception {
        when(companyLookupService.getCompanyProfile(COMPANY_NUMBER)).thenReturn(null);
        this.mockMvc
                .perform(post(COMPANY_LOOKUP_URL, FORWARD_URL_PARAM).param("companyNumber",
                        COMPANY_NUMBER))
                .andExpect(status().isOk())
                .andExpect(view().name(TEMPLATE));
    }

    @Test
    @DisplayName("Post Company Lookup - Absolute URL specified")
    void postCompanyLookupAbsoluteFail() throws Exception {
        this.mockMvc.perform(post(COMPANY_LOOKUP_URL, "http://0.0.0.0"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(ERROR_TEMPLATE));
    }

}