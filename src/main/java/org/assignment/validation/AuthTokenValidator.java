package org.assignment.validation;

import java.util.regex.Pattern;

public class AuthTokenValidator implements ObjectValidator {

    public static final Pattern AUTH_TOKEN = Pattern.compile("^Bearer [a-zA-Z0-9]+$");

    /**
     * Validate date in format "DD-MM-YYYY"
     *
     * @param s
     * @return
     */
    @Override
    public boolean validate(String s) {
        return AUTH_TOKEN.matcher(s).matches();
    }

}
