package org.assignment.validation;

public class StringValidator implements ObjectValidator {
    @Override
    public boolean validate(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r') return false;
        }
        return true;
    }
}
