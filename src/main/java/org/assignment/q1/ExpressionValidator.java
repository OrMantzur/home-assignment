package org.assignment.q1;

/**
 * Validates mathematical expressions for correct syntax before evaluation.
 */
public class ExpressionValidator {

    private static final char OPENED_PARENTHESES = '(';
    private static final char CLOSED_PARENTHESES = ')';

    /**
     * Validates the expression structure before evaluation.
     *
     * @param expression
     */
    public void validate(String expression) {
        if (expression.isEmpty()) return;

        // 1. Check Parentheses Balance
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') balance++;
            else if (c == ')') balance--;

            if (balance < 0) {
                throw new IllegalArgumentException("Invalid Syntax: Closing parenthesis without opening.");
            }
        }
        if (balance != 0) {
            throw new IllegalArgumentException("Invalid Syntax: Unbalanced parentheses.");
        }

        // 2. Check for Invalid Characters
        // Allowed: Digits, Letters, +, -, *, /, ^, (, )
        for (char c : expression.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Operator.isOperator(c) && c != OPENED_PARENTHESES && c != CLOSED_PARENTHESES) {
                throw new IllegalArgumentException(String.format("Invalid character found: '%c'", c));
            }
        }

        // 3. Check Ends with Binary Operator (Simple check)
        char lastChar = expression.charAt(expression.length() - 1);
        if ("*/^".indexOf(lastChar) >= 0) {
            throw new IllegalArgumentException("Expression cannot end with an operator.");
        }

        // 4. Check Starts with Binary Operator
        char firstChar = expression.charAt(0);
        if ("*/^".indexOf(firstChar) >= 0) {
            throw new IllegalArgumentException("Expression cannot start with a binary operator.");
        }
    }

}
