package uk.gov.companieshouse.lookup.helper;

import uk.gov.companieshouse.lookup.model.ForwardUrl;

public class PageTitleHelper {

    private static final String CONFIRMATION_STATEMENT_TITLE="title.formtype.confirmation-statement";
    private static final String DEFAULT_TITLE="company.number.title";


    public String getPageTitleFromForwardURL(ForwardUrl forward) {
        if(forward.getForward().contains("confirmation-statement")) {
             return CONFIRMATION_STATEMENT_TITLE;
        } else {
            return DEFAULT_TITLE;
        }
    }
}
