package com.catrak.exatip.partner.util;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

@Component
public class Utility {

    public StringBuilder validateMandField(Map<String, Object> fields) {
        StringBuilder response = new StringBuilder();
        for (Entry<String, Object> o : fields.entrySet()) {
            Object value = o.getValue();
            String key = o.getKey();

            if (value == null) {
                response.append(key + " should not be null");
                return response;
            }

            if (value instanceof String && ((String) value).isEmpty()) {
                response.append(key + " should not be empty");
                return response;
            }
        }
        return response;
    }

    public boolean hasAllDigits(String string) {
        return string.matches("^[0-9]*$");
    }

    public boolean hasAllAlphanumeric(String string) {
        return string.matches("^[a-zA-Z0-9]*$");
    }

    public String removeCharacters(String string) {
        return string.replaceAll("\\D", "");
    }

    public boolean hasMobileNumberValid(String string) {
        return string.matches("^[1-9][0-9]{7,14}$");
    }

    public boolean isEmailValidate(String string) {
        return string.matches(
                "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    }

    public String trimWhiteSpace(String string) {
        return string.trim().isEmpty() ? null : string.trim();
    }
}