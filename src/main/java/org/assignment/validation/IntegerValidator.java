package org.assignment.validation;

public class IntegerValidator implements ObjectValidator {
    @Override
    public boolean validate(String s) {
        if (s.isEmpty() || s.startsWith("\"")) return false;
        int i = (s.charAt(0) == '-') ? 1 : 0;
        if (i == 1 && s.length() == 1) return false;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
}
