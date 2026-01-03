package org.assignment.validation;

/**
 * Robust Date Validator for dd-mm-yyyy format.
 * Enforces calendar logic and strict quoting as per schema.
 */
public class DateValidator implements ObjectValidator {

    @Override
    public boolean validate(String s) {
        if (s == null) return false;

        // 1. Handle strict quoting requirement
        String clean = s;
        if (s.startsWith("\"") && s.endsWith("\"")) {
            if (s.length() != 12) return false; // "dd-mm-yyyy" = 12 chars
            clean = s.substring(1, 11);
        } else if (s.length() != 10) {
            return false;
        }

        // 2. Structural check (dd-mm-yyyy)
        if (clean.charAt(2) != '-' || clean.charAt(5) != '-') return false;
        if (!isDigits(clean, 0, 2) || !isDigits(clean, 3, 5) || !isDigits(clean, 6, 10)) return false;

        // 3. Fast integer parsing (no allocations for performance)
        int day = fastParseInt(clean, 0, 2);
        int month = fastParseInt(clean, 3, 5);
        int year = fastParseInt(clean, 6, 10);

        // 4. Calendar Logic Validation
        return isValidCalendarDate(day, month, year);
    }

    private boolean isValidCalendarDate(int d, int m, int y) {
        // Basic range checks
        if (m < 1 || m > 12 || d < 1 || y < 1000 || y > 9999) return false;

        // Days in each month
        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // Leap year logic for February
        if (m == 2 && isLeapYear(y)) {
            return d <= 29;
        }

        return d <= daysInMonth[m - 1];
    }

    private boolean isLeapYear(int y) {
        // A year is leap if divisible by 4, but not by 100, unless divisible by 400
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
    }

    private int fastParseInt(String s, int start, int end) {
        int res = 0;
        for (int i = start; i < end; i++) {
            res = res * 10 + (s.charAt(i) - '0');
        }
        return res;
    }

    private static boolean isDigits(String s, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
}