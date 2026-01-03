package org.assignment.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ListValidator implements ObjectValidator {

    public static final char EQUALS_DELIMITER = '=';

    /**
     * Validates a list structure which may contain nested lists and objects.
     * Uses an iterative BFS approach to traverse the structure.
     * For example: [1, 2, {"key": ["value1", "value2"]}, true]
     * We will use queue to manage the item we need to validate. In our case,
     * each item can be a primitive, a list, or an object:
     * Queue = [ "[1, 2, {"key": ["value1", "value2"]}, true]" ]
     * We will dequeue the first item, and check if it's a list, object, or primitive.
     * If it's a list or object, we will split its content and enqueue each element
     * back to the queue for further validation. If it's a primitive, we will validate
     * it directly. This process continues until the queue is empty.
     * After the first processing of the queue, the queue will look like this:
     * Queue = [ "1", "2", "{"key": ["value1", "value2"]}", "true" ]
     * Second processing:
     * Queue = [ "{"key": ["value1", "value2"]}" ]
     * Third processing:
     * Queue = [ "["value1", "value2"]" ]
     * Fourth processing:
     * Queue = [ "value1", "value2" ]
     * Final processing:
     * Queue = [ ]
     *
     * @param s the input string representing the list structure.
     * @return true if the input string is a valid list structure, false otherwise.
     */
    @Override
    public boolean validate(String s) {
        String input = s.trim();
        if (input.length() < 2 || input.charAt(0) != '[' || input.charAt(input.length() - 1) != ']') return false;

        // BFS Queue approach for iterative structure traversal
        Queue<String> queue = new ArrayDeque<>();
        queue.add(input);

        while (!queue.isEmpty()) {
            String current = queue.poll().trim();
            if (current.isEmpty()) return false;

            if (current.startsWith("[")) {
                if (!current.endsWith("]")) return false;
                String content = current.substring(1, current.length() - 1).trim();
                if (content.isEmpty()) continue;
                List<String> elements = smartSplit(content, ',');
                if (elements == null) return false;
                queue.addAll(elements);
            } else if (current.startsWith("{")) {
                if (!current.endsWith("}")) return false;
                String content = current.substring(1, current.length() - 1).trim();
                if (content.isEmpty()) continue;
                List<String> pairs = smartSplit(content, ',');
                if (pairs == null) return false;
                for (String pair : pairs) {
                    List<String> kv = smartSplit(pair, EQUALS_DELIMITER);
                    if (kv == null || kv.size() != 2) return false;
                    queue.add(kv.get(1).trim());
                }
            } else {
                // Enforces that items inside the list follow strict quoting rules
                if (!validateAsPrimitiveInContext(current)) return false;
            }
        }
        return true;
    }

    /**
     * Splits a string by the given delimiter, ignoring delimiters inside nested structures or quotes.
     * Returns null if unbalanced structures or quotes are detected.
     */
    private static List<String> smartSplit(String s, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"') inQuotes = !inQuotes;
            if (!inQuotes) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
            }
            if (c == delimiter && depth == 0 && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (depth != 0 || inQuotes) return null;
        result.add(sb.toString().trim());
        return result;
    }

    /**
     * Validates that a primitive item is valid in the context of being inside a list or object.
     * Enforces stricter rules than standalone validation.
     */
    private static boolean validateAsPrimitiveInContext(String item) {
        if (item.startsWith("\"") && item.endsWith("\"")) {
            item = item.substring(1, item.length() - 1);
            // Must be one of the String-based types defined in the table
            return ValueType.STRING.isValid(item) || ValueType.DATE.isValid(item) || ValueType.EMAIL.isValid(item) ||
                    ValueType.UUID.isValid(item) || ValueType.AUTH_TOKEN.isValid(item);
        }
        // Unquoted: Only raw primitives allowed
        return ValueType.INT.isValid(item) || ValueType.BOOLEAN.isValid(item);
    }

}
