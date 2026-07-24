package uk.gov.companieshouse.lookup.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;

import uk.gov.companieshouse.sdk.manager.ApiClientManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    private static final String CHS_API_KEY = "CHS_API_KEY";
    private static final String TEST_API_KEY = "test-api-key";

    @Mock
    private EnvironmentReader environmentReader;

    @InjectMocks
    private ApiClientServiceImpl apiClientService;

    @Test
    @DisplayName("Default constructor creates instance with EnvironmentReaderImpl")
    void defaultConstructorCreatesInstance() {
        ApiClientServiceImpl service = new ApiClientServiceImpl();
        assertNotNull(service);
    }

    @Test
    @DisplayName("getApiClient uses API key authentication rather than OAuth session")
    void getApiClientUsesApiKeyAuthentication() {
        when(environmentReader.getMandatoryString(CHS_API_KEY)).thenReturn(TEST_API_KEY);

        try (MockedStatic<ApiClientManager> mockedManager = mockStatic(ApiClientManager.class)) {
            ApiClient mockClient = mock(ApiClient.class);
            mockedManager.when(() -> ApiClientManager.getSDK(TEST_API_KEY)).thenReturn(mockClient);

            ApiClient result = apiClientService.getApiClient();

            assertNotNull(result);
            mockedManager.verify(() -> ApiClientManager.getSDK(TEST_API_KEY));
        }

        verify(environmentReader).getMandatoryString(CHS_API_KEY);
    }
}
