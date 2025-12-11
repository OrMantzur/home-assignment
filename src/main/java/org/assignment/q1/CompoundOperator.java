package org.assignment.q1;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum representing compound assignment operators.
 */
@Getter
public enum CompoundOperator {

    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/=");

    private final String symbol;

    // --- 1. Optimization: Static Set for O(1) Lookup ---
    private static final Map<String, CompoundOperator> LOOKUP_MAP = new HashMap<>();

    static {
        for (CompoundOperator op : values()) {
            LOOKUP_MAP.put(op.symbol, op);
        }
    }

    CompoundOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns a list of all valid compound assignment symbols.
     * Useful for validation or regex building.
     *
     * @return A list containing ["+=", "-=", "*=", "/="]
     */
    public static List<String> getAllSymbols() {
        List<String> symbols = new ArrayList<>();
        for (CompoundOperator op : values()) {
            symbols.add(op.symbol);
        }
        return symbols;
    }

    /**
     * Gets the Operator enum corresponding to the given symbol.
     *
     * @param symbol
     * @return
     */
    public static CompoundOperator get(String symbol) {
        CompoundOperator op = LOOKUP_MAP.get(symbol);
        if (op == null) {
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
        return op;
    }

}
