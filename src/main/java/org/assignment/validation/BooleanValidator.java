package org.assignment.validation;

public class BooleanValidator implements ObjectValidator {

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    @Override
    public boolean validate(String s) {
        return TRUE.equals(s) || FALSE.equals(s);
    }

}
