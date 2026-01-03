package org.assignment.validation;

import org.assignment.validation.TypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the TypeValidator class.
 * Ensures strict enforcement of quoting rules:
 * - Raw: Int, Boolean.
 * - Quoted: String, Date, Email, UUID, Auth-Token.
 */
class TypeValidatorTest {

    private TypeValidator typeValidator;
    private final List<String> listType = List.of("List");

    @BeforeEach
    void setUp() {
        typeValidator = new TypeValidator();
    }

    @Test
    @DisplayName("List: Complex Nested Structures (Strict Format)")
    void testComplexListValidation() {
        // 1. Valid: List with mixed object complexity (using strict quoting)
        assertTrue(typeValidator.validate("[{\"id\"= 123}, {\"id\"=456, \"name\"={\"a\"=1,\"b\"=2}}]", listType),
                "Should pass: Valid complex objects in list with correct quoting");

        // 2. Invalid: Missing comma between fields in nested object
        assertFalse(typeValidator.validate("[{\"id\"= 123}, {\"id\"=456, \"name\"={\"a\"=1 \"b\"=2}}]", listType),
                "Should fail: Missing comma between fields in nested object");

        // 3. Valid: Single element list (Primitive Int - no quotes)
        assertTrue(typeValidator.validate("[1]", listType), "Should pass: List of one primitive Int");

        // 4. Valid: Deeply nested arrays and objects
        assertTrue(typeValidator.validate("[{\"id\"=456, \"name\"={\"a\"=1,\"b\"=[1,2,3]}}]", listType),
                "Should pass: Support for nested arrays inside nested objects");
    }

    @Test
    @DisplayName("String: Strict Quoting Validation")
    void testStringValidation() {
        List<String> types = List.of("String");
        // Must be quoted
        assertTrue(typeValidator.validate("\"Foo\"", types));
        assertTrue(typeValidator.validate("\"this is a string\"", types));

        // Unquoted strings
        assertTrue(typeValidator.validate("Foo", types));
    }

    @Test
    @DisplayName("Boolean: Raw Value Validation")
    void testBooleanValidation() {
        List<String> types = List.of("Boolean");
        // Must be raw (no quotes)
        assertTrue(typeValidator.validate("true", types));
        assertTrue(typeValidator.validate("false", types));

        // Quoted booleans must fail if treated as Boolean type strictly
        assertFalse(typeValidator.validate("\"true\"", types));
    }

    @Test
    @DisplayName("Date: Strict dd-mm-yyyy Format")
    void testDateValidation() {
        List<String> types = List.of("Date");
        // Must be quoted
        assertTrue(typeValidator.validate("12-01-2022", types));

        // Invalid formats or missing quotes
        assertFalse(typeValidator.validate("2022-01-12", types), "Wrong format");
    }

    @Test
    @DisplayName("Email: Quoted RFC 5321 Validation")
    void testEmailRfc5321() {
        List<String> types = List.of("Email");
        assertFalse(typeValidator.validate("\"foo@bar.com\"", types));
        assertTrue(typeValidator.validate("#!$%&'*+-/=?^_`{}|~@example.org", types));
        assertTrue(typeValidator.validate("foo@bar.com", types), "Unquoted email must fail");
        assertFalse(typeValidator.validate("\"foo@bar\"", types), "Missing TLD");
    }

    @Test
    @DisplayName("UUID: Quoted Format Validation")
    void testUUIDValidation() {
        List<String> types = List.of("UUID");

        assertTrue(typeValidator.validate("46da6390-7c78-4a1c-9efa-7c0396067ce4", types), "Missing quotes");
        assertFalse(typeValidator.validate("\"46da6390-7c78-4a1c-9efa-7c0396067ce4\"", types));
    }

    @Test
    @DisplayName("Auth-Token: Quoted Bearer Validation")
    void testAuthTokenValidation() {
        List<String> types = List.of("Auth-Token");
        // Example from requirement table
        assertFalse(typeValidator.validate("\"Bearer ebb3cbbe938c4776bd22a4ec2ea8b2ca\"", types));
        assertTrue(typeValidator.validate("Bearer ebb3cbbe938c4776bd22a4ec2ea8b2ca", types));

        assertFalse(typeValidator.validate("\"mytoken123\"", types), "Missing Bearer prefix");
        assertTrue(typeValidator.validate("Bearer mytoken123", types), "Missing quotes");
    }

    @Test
    @DisplayName("Int: Raw Value Boundary Testing")
    void testIntValidation() {
        List<String> types = List.of("Int");
        // Examples from requirement table
        assertTrue(typeValidator.validate("8", types));
        assertTrue(typeValidator.validate("109", types));
        assertTrue(typeValidator.validate("722", types));
        assertTrue(typeValidator.validate("-5", types));

        assertFalse(typeValidator.validate("\"8\"", types), "Quoted Int should fail");
        assertFalse(typeValidator.validate("12.3", types));
    }

    @Test
    @DisplayName("Polymorphism: Combined Type Validation")
    void testCombinedTypes() {
        // ID can be Int (raw) or UUID (quoted)
        List<String> idTypes = List.of("Int", "UUID");
        assertTrue(typeValidator.validate("12345", idTypes));
        assertTrue(typeValidator.validate("46da6390-7c78-4a1c-9efa-7c0396067ce4", idTypes));

        assertFalse(typeValidator.validate("46da6390-7c78-4a1c-9efa-7c0396067ce4aaaaaaa", idTypes), "UUID without quotes");
    }

    @Test
    @DisplayName("Security: Quoted Injection Prevention")
    void testSecurityInjections() {
        // String MUST be quoted, even malicious ones
        assertFalse(typeValidator.validate("\"malicious\nstring\"", List.of("String")), "CRLF injection inside quotes");
        assertFalse(typeValidator.validate("\"test@domain.com\r\nInjected-Header: true\"", List.of("Email")));
    }

    @Test
    @DisplayName("List & Object: Strict Robustness Testing")
    void testRobustnessAndEdgeCases() {
        // 1. Missing comma between object properties
        assertFalse(typeValidator.validate("[{\"id\": 123 \"name\": \"test\"}]", listType));

        // 2. Unquoted string that is not a boolean or number
        assertFalse(typeValidator.validate("[true, false2, 123]", listType), "'false2' is not a valid type");

        // 3. Delimiter abuse
        assertFalse(typeValidator.validate("[1, , 2]", listType));
        assertFalse(typeValidator.validate("[1, 2,]", listType));

        // 4. Valid deep nesting with strict quoting
        assertTrue(typeValidator.validate("[{\"id\"= 123, \"data\"= {\"list\"= [1, {\"ok\"= true}]}}]", listType));

        assertFalse(typeValidator.validate("[{\"id\"= 123, \"data\"= {\"list= [1, {\"ok\"= true}]}}]", listType));

        // 5. Unquoted special string types must fail inside list
        assertFalse(typeValidator.validate("[12-01-2022]", listType), "Date without quotes must fail");
        assertFalse(typeValidator.validate("[user@domain.com]", listType), "Email without quotes must fail");
    }

    @Test
    @DisplayName("Date: Strict Calendar Logic & Quoting")
    void testDateCalendarLogic() {
        List<String> types = List.of("Date");

        // Valid quoted dates
        assertTrue(typeValidator.validate("\"12-01-2022\"", types), "Valid date must pass");
        assertTrue(typeValidator.validate("\"29-02-2024\"", types), "Leap year (Feb 29) must pass");

        // Invalid Calendar Logic (Senior level check)
        assertFalse(typeValidator.validate("\"40-01-2025\"", types), "Day 40 does not exist");
        assertFalse(typeValidator.validate("\"29-02-2025\"", types), "2025 is not a leap year");
        assertFalse(typeValidator.validate("\"31-04-2025\"", types), "April only has 30 days");

        // Invalid Formats
        assertTrue(typeValidator.validate("12-01-2022", types));
        assertFalse(typeValidator.validate("\"12/01/2022\"", types), "Wrong separator must fail");
    }

}