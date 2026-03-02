package uk.gov.companieshouse.lookup.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.lookup.model.ForwardUrl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PageTitleHelperTest {

    private final static String CS01_FORWARD_URL="/confirmation-statement/confirm-company?companyNumber={companyNumber}";
    private final static String DEFAULT_FORWARD_URL="/random-service/confirm-company?companyNumber={companyNumber}";

    private final PageTitleHelper titleHelper = new PageTitleHelper();

    @Test
    @DisplayName("Get Page Title - Confirmation Statement forward URL")
    void testPageTitleContainsConfirmationStatementWhenInForwardURL() {
        ForwardUrl url = new ForwardUrl(CS01_FORWARD_URL);

        String title = titleHelper.getPageTitleFromForwardURL(url);
        // Assert
        assertTrue(title.contains("confirmation-statement"));
    }

    @Test
    @DisplayName("Get Page Title - any other forward URL")
    void testPageTitleIsDefaultWhenNoformTypeFound() {
        ForwardUrl url = new ForwardUrl(DEFAULT_FORWARD_URL);

        String title = titleHelper.getPageTitleFromForwardURL(url);
        // Assert
        assertEquals("company.number.title", title);
    }
}
