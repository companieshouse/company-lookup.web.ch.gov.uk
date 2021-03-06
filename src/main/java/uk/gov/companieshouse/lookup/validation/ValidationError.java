package uk.gov.companieshouse.lookup.validation;

import java.util.Map;


public class ValidationError {

    private String messageKey;
    private Map<String, String> messageArguments;
    private String fieldPath;

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public Map<String, String> getMessageArguments() {
        return messageArguments;
    }

    public void setMessageArguments(Map<String, String> messageArguments) {
        this.messageArguments = messageArguments;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }
}
