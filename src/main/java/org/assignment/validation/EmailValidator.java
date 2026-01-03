package org.assignment.validation;

import java.util.regex.Pattern;

public class EmailValidator implements ObjectValidator {

    public static final Pattern EMAIL_RFC5321 = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$"
    );

    /**
     * Validates if the given string is a valid email address enclosed in double quotes.
     *
     * @param s
     * @return
     */
    @Override
    public boolean validate(String s) {
        return s.length() <= 255 && EMAIL_RFC5321.matcher(s).matches();
    }

}