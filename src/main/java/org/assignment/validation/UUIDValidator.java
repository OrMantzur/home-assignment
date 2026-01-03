package org.assignment.validation;

import java.util.regex.Pattern;

public class UUIDValidator implements ObjectValidator {

    public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    /**
     * Validates if the given string is a valid email address enclosed in double quotes.
     *
     * @param s
     * @return
     */
    @Override
    public boolean validate(String s) {
        if (s.length() != 36) return false;
        return UUID.matcher(s).matches();
    }

}