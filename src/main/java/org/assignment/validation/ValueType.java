package org.assignment.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Enum representing supported value types with strict schema enforcement.
 * - Int & Boolean: No quotes.
 * - String & Subtypes (Date, Email, etc.): Mandatory quotes.
 * - List: BFS iterative validation.
 */
public enum ValueType {

    INT("Int", s -> new IntegerValidator().validate(s)),
    STRING("String", s -> new StringValidator().validate(s)),
    BOOLEAN("Boolean", s -> new BooleanValidator().validate(s)),
    LIST("List", s -> new ListValidator().validate(s)),
    DATE("Date", s -> new DateValidator().validate(s)),
    EMAIL("Email", s -> new EmailValidator().validate(s)),
    UUID("UUID", s -> new UUIDValidator().validate(s)),
    AUTH_TOKEN("Auth-Token", s -> new AuthTokenValidator().validate(s));

    private static final Map<String, ValueType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(v -> v.typeName, v -> v));

    private final String typeName;
    private final Predicate<String> validator;

    ValueType(String typeName, Predicate<String> validator) {
        this.typeName = typeName;
        this.validator = validator;
    }

    public boolean isValid(String value) {
        return value != null && validator.test(value);
    }

    public static ValueType get(String typeName) {
        return LOOKUP.get(typeName);
    }

    public static boolean isSupportedType(String typeName) {
        return typeName != null && LOOKUP.containsKey(typeName);
    }

}