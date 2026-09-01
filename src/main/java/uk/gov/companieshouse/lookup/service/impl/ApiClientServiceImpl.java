package uk.gov.companieshouse.lookup.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.lookup.service.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

@Service
public class ApiClientServiceImpl implements ApiClientService {

    private static final String CHS_API_KEY = "CHS_API_KEY";

    private final EnvironmentReader environmentReader;

    @Autowired
    public ApiClientServiceImpl() {
        this(new EnvironmentReaderImpl());
    }

    ApiClientServiceImpl(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    @Override
    public ApiClient getApiClient() {
        return ApiClientManager.getSDK(environmentReader.getMandatoryString(CHS_API_KEY));
    }
}

