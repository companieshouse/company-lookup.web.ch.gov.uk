package uk.gov.companieshouse.lookup.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LinkUtilsTest {

    private final LinkUtils linkUtils = new LinkUtils();

    @DisplayName("Accepts safe relative links")
    @ParameterizedTest(name = "{index} => {0}: {1}")
    @CsvSource(delimiter = '|', textBlock = """
            root path   | /confirmation-statement/
            nested path | /confirmation-statement/confirm
            root only   | /
            """)
    void isSafeRelativeLinkReturnsTrueForValidPaths(String testCase, String path) {
        assertTrue(linkUtils.isSafeRelativeLink(path));
        assertTrue(linkUtils.resolveRelativeLink(path).isPresent());
    }

    @DisplayName("Rejects unsafe relative links")
    @ParameterizedTest(name = "{index} => {0}: {1}")
    @CsvSource(delimiter = '|', nullValues = "NULL", textBlock = """
            null link           | NULL
            blank link          |
            absolute url        | https://example.com/confirmation-statement
            scheme relative url | //example.com/confirmation-statement
            no leading slash    | confirmation-statement/confirm
            """)
    void isSafeRelativeLinkReturnsFalseForInvalidPaths(String testCase, String path) {
        assertFalse(linkUtils.isSafeRelativeLink(path));
        assertTrue(linkUtils.resolveRelativeLink(path).isEmpty());
    }

    @Test
    void resolveRelativeLinkReturnsExpectedOptionalValue() {
        String validPath = "/confirmation-statement/confirm";
        assertTrue(linkUtils.resolveRelativeLink(validPath).isPresent());
        assertEquals(validPath, linkUtils.resolveRelativeLink(validPath).get());
        assertTrue(linkUtils.resolveRelativeLink("https://example.com").isEmpty());
    }
}
