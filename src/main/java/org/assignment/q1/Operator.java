package org.assignment.q1;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing basic mathematical operators.
 */
@Getter
public enum Operator {

    PLUS('+'),
    MINUS('-'),
    MULTIPLY('*'),
    DIVIDE('/'),
    POWER('^');

    private final char symbol;

    // --- 1. Optimization: Static Set for O(1) Lookup ---
    private static final Map<Character, Operator> LOOKUP_MAP = new HashMap<>();

    static {
        for (Operator op : values()) {
            LOOKUP_MAP.put(op.symbol, op);
        }
    }

    Operator(char symbol) {
        this.symbol = symbol;
    }

    /**
     * Overload for char (convenience).
     * Usage: Operator.isOperator('+') -> true
     */
    public static boolean isOperator(char symbol) {
        return LOOKUP_MAP.containsKey(symbol);
    }

    /**
     * Checks if the given symbol is a high priority math operator (*, /, ^).
     *
     * @param symbol
     * @return
     */
    public static boolean highPriorityMathOperator(char symbol) {
        return symbol == MULTIPLY.symbol || symbol == DIVIDE.symbol || symbol == POWER.symbol;
    }

    /**
     * Gets the Operator enum corresponding to the given symbol.
     *
     * @param symbol
     * @return
     */
    public static Operator get(char symbol) {
        Operator op = LOOKUP_MAP.get(symbol);
        if (op == null) {
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
        return op;
    }

    /**
     * Checks if the given character is a mathematical operator.
     *
     * @param c The character to check.
     * @return true if the character is a mathematical operator, false otherwise.
     */
    public static boolean isMathChar(char c) {
        return c == PLUS.symbol || c == MINUS.symbol || c == MULTIPLY.symbol
                || c == DIVIDE.symbol || c == POWER.symbol;
    }

}
