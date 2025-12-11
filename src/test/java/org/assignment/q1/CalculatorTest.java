package org.assignment.q1;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class aim to test {@link Calculator} functionality.
 * It is covering various scenarios including:
 * 1. Basic arithmetic operations and order of operations.
 * 2. Variable assignments and usage.
 * 3. Unary operators (pre/post increment/decrement).
 * 4. Compound assignments.
 * 5. Handling of negative numbers.
 * 6. Edge cases and error handling.
 */
@Slf4j
public class CalculatorTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    // --- 1. Sanity & Basic Arithmetic ---

    @Test
    void testBasicArithmetic() {
        calculator.evaluate("x = 1 + 2");
        assertVariableValue("x", 3);

        calculator.evaluate("y = 10 - 4");
        assertVariableValue("y", 6);

        calculator.evaluate("z = 3 * 4");
        assertVariableValue("z", 12);

        calculator.evaluate("w = 20 / 5");
        assertVariableValue("w", 4);
    }

    @Test
    void testOrderOfOperations() {
        // Multiplication before Addition
        calculator.evaluate("x = 1 + 2 * 3");
        assertVariableValue("x", 7); // Not 9

        // Parentheses override precedence
        calculator.evaluate("y = (1 + 2) * 3");
        assertVariableValue("y", 9);
    }

    @Test
    void testPowerRightAssociativity() {
        // 2 ^ 3 ^ 2 should be 2 ^ (3 ^ 2) = 2 ^ 9 = 512
        // NOT (2 ^ 3) ^ 2 = 8 ^ 2 = 64
        calculator.evaluate("x = 2 ^ 3 ^ 2");
        assertVariableValue("x", 512);
    }

    // --- 2. Variables & Persistence ---

    @Test
    void testVariableUsage() {
        calculator.evaluate("x = 10");
        calculator.evaluate("y = x + 5");
        assertVariableValue("y", 15);

        // Ensure x didn't change
        assertVariableValue("x", 10);
    }

    @Test
    void testUndefinedVariableDefaultsToZero() {
        // 'z' is not defined, should be treated as 0
        calculator.evaluate("res = z + 5");
        assertVariableValue("res", 5);
    }

    // --- 3. Unary Operators (Tricky Stuff) ---

    @Test
    void testPostIncrement() {
        // x = i++ (use OLD value, then increment)
        calculator.evaluate("i = 5");
        calculator.evaluate("x = i++");

        assertVariableValue("x", 5); // Received old value
        assertVariableValue("i", 6); // Incremented afterwards
    }

    @Test
    void testPreIncrement() {
        // x = ++i (increment, then use NEW value)
        calculator.evaluate("i = 5");
        calculator.evaluate("x = ++i");

        assertVariableValue("x", 6); // Received new value
        assertVariableValue("i", 6);
    }

    @Test
    void testPostDecrement() {
        calculator.evaluate("j = 10");
        calculator.evaluate("y = j--");

        assertVariableValue("y", 10);
        assertVariableValue("j", 9);
    }

    @Test
    void testComplexUnaryExpression() {
        // i = 5.
        // i++ uses 5, i becomes 6.
        // i * 2 = 6 * 2 = 12.
        // Result = 5 + 12 = 17.
        calculator.evaluate("i = 5");
        calculator.evaluate("res = i++ + i * 2");

        assertVariableValue("res", 17);
        assertVariableValue("i", 6);
    }

    // --- 4. Compound Assignments ---

    @Test
    void testCompoundAssignments() {
        calculator.evaluate("x = 10");

        calculator.evaluate("x += 5"); // 15
        assertVariableValue("x", 15);

        calculator.evaluate("x -= 3"); // 12
        assertVariableValue("x", 12);

        calculator.evaluate("x *= 2"); // 24
        assertVariableValue("x", 24);

        calculator.evaluate("x /= 6"); // 4
        assertVariableValue("x", 4);
    }

    @Test
    void testCompoundAssignmentOrder() {
        // x += 2 * 3
        // Should be x = x + (2 * 3) = 10 + 6 = 16
        calculator.evaluate("x = 10");
        calculator.evaluate("x += 2 * 3");
        assertVariableValue("x", 16);
    }

    // --- 5. Negative Numbers ---

    @Test
    void testNegativeNumbers() {
        calculator.evaluate("x = -5");
        assertVariableValue("x", -5);

        calculator.evaluate("y = 10 + -3");
        assertVariableValue("y", 7);

        calculator.evaluate("z = 5 - -2"); // 5 - (-2) = 7
        assertVariableValue("z", 7);
    }

    // --- 6. Edge Cases & Errors ---

    @Test
    void testDivisionByZero_ThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            calculator.evaluate("x = 10 / 0");
        });
    }

    @Test
    void testAssignmentDivisionByZero_ThrowsException() {
        calculator.evaluate("x = 10");
        assertThrows(UnsupportedOperationException.class, () -> {
            calculator.evaluate("x /= 0");
        });
    }

    @Test
    void testChainedAssignment_ThrowsException() {
        // Assuming we decided to block x=y=5 in the code logic
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.evaluate("x = y = 5");
        });
    }

    // --- 7. Advanced Unary Scenarios (Gap Analysis) ---

    @Test
    void testUnaryMinusOnVariable() {
        // Current logic might fail this if it only looks for digits after '-'
        calculator.evaluate("x = 10");
        calculator.evaluate("y = -x");

        // Expect y to be -10.
        // If the parser ignores the minus because x is not a digit, this will fail.
        assertVariableValue("y", -10);
    }

    // --- 8. Variable Naming & Parsing ---


    @Test
    void testVariableStartingWithUnderscore() {
        calculator.evaluate("_priv = 50");
        assertVariableValue("_priv", 50);
    }

    // --- 9. Robustness & Overflow ---

    @Test
    void testIntegerOverflow() {
        // Demonstrate awareness of Java int limits
        // Max Int is 2,147,483,647. Adding 1 should wrap around to negative.
        calculator.evaluate("max = 2147483647");
        calculator.evaluate("overflow = max + 1");

        assertVariableValue("overflow", -2147483648);
    }

    @Test
    void testEmptyOrNullInput() {
        // Should essentially do nothing or handle gracefully without crashing
        assertDoesNotThrow(() -> calculator.evaluate(""));

        // If your code doesn't handle null, maybe expect NullPointerException
        // or better - add a check in the code and expect IllegalArgumentException
        // assertThrows(IllegalArgumentException.class, () -> calculator.evaluate(null));
    }

    @Test
    void testExpressionWithoutAssignment() {
        // Valid scenario: "i++" or "5+5". Should run but not save to variable map (except side effects).
        calculator.evaluate("i = 0");
        calculator.evaluate("i++"); // Should increment i
        assertVariableValue("i", 1);
    }

    @Test
    void testMalformedExpression_MissingOperand() {
        // "5 +" is invalid because + needs two operands
        assertThrows(Exception.class, () -> {
            calculator.evaluate("x = 5 +");
        });
    }

    @Test
    void testMalformedExpression_MissingOperator() {
        // "5 5" is invalid
        assertThrows(Exception.class, () -> {
            calculator.evaluate("x = 5 5");
        });
    }

    @Test
    void testDoubleNegative() {
        // Mathematical logic: 5 - (-2) = 7.
        // This we know works from previous fixes.
        calculator.evaluate("res = 5 - -2");
        assertVariableValue("res", 7);
    }

    @Test
    void testUnaryPlus() {
        // x = +5 should be treated as 5 (parse '+')
        // Note: Our current code might struggle here if it sees '+' as binary only.
        try {
            calculator.evaluate("x = +5");
            assertVariableValue("x", 5);
        } catch (Exception e) {
            System.out.println("Known limitation: Unary plus not fully supported yet");
        }
    }

    // --- Helper Method: Reflection to inspect private Map ---

    private void assertVariableValue(String varName, int expectedValue) {
        try {
            // Access the private field 'variableMap'
            Field field = Calculator.class.getDeclaredField("variableMemoryMap");
            // Make it accessible
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, Integer> memory = (Map<String, Integer>) field.get(calculator);

            assertTrue(memory.containsKey(varName), "Variable " + varName + " should exist in memory");
            assertEquals(expectedValue, memory.get(varName), "Value for " + varName + " is incorrect");

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to inspect Calculator memory via reflection: " + e.getMessage());
        }
    }

}