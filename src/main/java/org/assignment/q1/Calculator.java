package org.assignment.q1;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Text-based Calculator Implementation.
 * * Strategy: Separation of Assignment (L-Value) and Math Evaluation (R-Value).
 * * This approach simplifies the problem by splitting the line into two parts:
 * 1. The variable to update (Left side).
 * 2. The mathematical expression to calculate (Right side).
 * * Supports:
 * - Math: +, -, *, /, ^ (Power)
 * - Assignments: =, +=, -=, *=, /=
 * - Unary Operators: i++, ++i, i--, --i
 * - Parentheses: ( )
 * - Validation: Detects malformed expressions.
 */
@Slf4j
public class Calculator {

    private static final String SPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final String SPACE_BETWEEN_DIGIT_REGEX = ".*\\d\\s+\\d.*";
    private static final String EQUALS = "=";
    private static final char OPENED_PARENTHESES = '(';
    private static final char CLOSED_PARENTHESES = ')';

    // Program Memory: Maps variable names (e.g., "count") to their integer values.
    private final Map<String, Integer> variableMemoryMap = new HashMap<>();
    private final ExpressionValidator expressionValidator = new ExpressionValidator();

    /**
     * Evaluates a given expression string.
     * Main entry point.
     * Parses a line of code, handles the assignment logic, and triggers math evaluation.
     * Example input: "x += 5 * 2"
     *
     * @param expression
     */
    public void evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }

        // Validation: Check for spaces between numbers (e.g., "5 5") BEFORE stripping spaces.
        // If we don't check this, "5 5" becomes "55" which is valid but wrong.
        // This regex looks for a digit followed by spaces followed by another digit.
        if (expression.matches(SPACE_BETWEEN_DIGIT_REGEX)) {
            throw new IllegalArgumentException("Invalid Expression: Numbers cannot be separated by spaces (e.g., '5 5').");
        }

        // Step 1: Pre-process the input to remove spaces.
        // This simplifies parsing multi-character operators (like "+=" or "++").
        String sanitizedExpression = expression.replace(SPACE, EMPTY_STRING);

        // Step 2: Detect the Assignment Operator (=, +=, -=, *=, /=).
        // We look for the operator that splits the Left-Value from the Right-Value.
        String assignmentOperator = null;
        int assignmentSplitIndex = -1;

        // Priority 1: Check for compound assignments (2 characters long)
        for (String operator : CompoundOperator.getAllSymbols()) {
            int index = sanitizedExpression.indexOf(operator);
            if (index != -1) {
                assignmentOperator = operator;
                assignmentSplitIndex = index;
                break;
            }
        }

        // Priority 2: If no compound operator found, check for simple assignment '='
        if (assignmentOperator == null) {
            int index = sanitizedExpression.indexOf(EQUALS);
            if (index != -1) {
                assignmentOperator = EQUALS;
                assignmentSplitIndex = index;
            }
        }

        // Step 3: Execute Logic
        if (assignmentOperator != null) {
            // Case A: Assignment detected (e.g., "x = 5 + 3")

            // Extract the variable name (Left Side)
            String targetVariableName = sanitizedExpression.substring(0, assignmentSplitIndex);

            // Extract the math expression (Right Side)
            String mathExpression = sanitizedExpression.substring(assignmentSplitIndex + assignmentOperator.length());

            // Validation: Ensure we don't have chained assignments like "x = y = 5" which are not supported in this simplified version
            if (mathExpression.contains(EQUALS)) {
                throw new IllegalArgumentException("Chained assignments (e.g., x=y=z) are not supported in this implementation.");
            }

            // [VALIDATION STEP] Validate syntax before calculating
            expressionValidator.validate(mathExpression);

            // Calculate the value of the right side expression
            int calculatedResult = evaluateMathExpression(mathExpression);

            // Update the variable in memory
            performAssignment(targetVariableName, assignmentOperator, calculatedResult);
        } else {
            // Case B: No assignment detected (e.g., "i++" or just "5 + 2")

            // [VALIDATION STEP] Validate syntax before calculating
            expressionValidator.validate(sanitizedExpression);

            // We just evaluate the expression (side effects like i++ will still happen inside).
            evaluateMathExpression(sanitizedExpression);
        }

        // Step 4: Print final state as required
        printMemoryState();
    }

    /**
     * Pure Math Evaluator using Dijkstra's Two-Stack Algorithm.
     * * Responsibilities:
     * 1. Evaluate mathematical order of operations (+, -, *, /, ^).
     * 2. Handle Parentheses.
     * 3. Handle Unary Operators (i++, ++i, i--, --i) which modify the memory map as side effects.
     * * @param expression The pure math string (e.g., "i++ + 5 * 2")
     *
     * @return The integer result of the calculation.
     */
    private int evaluateMathExpression(String expression) {
        Deque<Integer> valueStack = new ArrayDeque<>();
        Deque<Character> operatorStack = new ArrayDeque<>();
        char[] expressionChars = expression.toCharArray();

        for (int i = 0; i < expressionChars.length; i++) {
            char currentChar = expressionChars[i];

            // --- Case 1: Negative Values (Unary Minus) ---
            // Handles cases like "-5" or "5 * -2" OR "-x"
            // Condition: Current is '-', Next is Digit OR Letter
            char minusSymbol = Operator.MINUS.getSymbol();
            char plusSymbol = Operator.PLUS.getSymbol();
            if (currentChar == minusSymbol && i + 1 < expressionChars.length) {
                char nextChar = expressionChars[i + 1];
                if (Character.isDigit(nextChar) || Character.isLetter(nextChar)) {

                    boolean isStart = (i == 0);
                    // Check if previous char was an operator or parenthesis
                    // (meaning this minus is unary like in "-3 + 5" and not like "5 - 3")
                    boolean prevIsOperator = (i > 0 && (Operator.isMathChar(expressionChars[i - 1]) || expressionChars[i - 1] == OPENED_PARENTHESES));

                    if (isStart || prevIsOperator) {
                        i++; // Consume the '-'

                        // Sub-Case A: Negative Number ("-5")
                        if (Character.isDigit(nextChar)) {
                            StringBuilder numberBuilder = new StringBuilder();
                            numberBuilder.append(minusSymbol);
                            while (i < expressionChars.length && Character.isDigit(expressionChars[i])) {
                                numberBuilder.append(expressionChars[i++]);
                            }
                            valueStack.push(Integer.parseInt(numberBuilder.toString()));
                        }
                        // Sub-Case B: Negative Variable ("-x")
                        else {
                            StringBuilder variableBuilder = new StringBuilder();
                            // Collect variable name (Letter/Digit only)
                            while (i < expressionChars.length && Character.isLetterOrDigit(expressionChars[i])) {
                                variableBuilder.append(expressionChars[i++]);
                            }
                            String varName = variableBuilder.toString();
                            int val = variableMemoryMap.getOrDefault(varName, 0);
                            // Push negated value
                            valueStack.push(-val);
                        }

                        // Step back after loop
                        i--;
                        // Skip the rest of the loop
                        continue;
                    }
                }
            }

            // --- Case 2: Numbers ---
            if (Character.isDigit(currentChar)) {
                StringBuilder numberBuilder = new StringBuilder();
                while (i < expressionChars.length && Character.isDigit(expressionChars[i])) {
                    numberBuilder.append(expressionChars[i++]);
                }
                valueStack.push(Integer.parseInt(numberBuilder.toString()));
                // Step back after loop
                i--;
            }

            // --- Case 3: Variables & Post-Fix Operators (i++, i--) ---
            else if (Character.isLetter(currentChar)) {
                // Get variable name
                StringBuilder variableBuilder = new StringBuilder();
                while (i < expressionChars.length && Character.isLetterOrDigit(expressionChars[i])) {
                    variableBuilder.append(expressionChars[i++]);
                }
                String variableName = variableBuilder.toString();

                // Check for Post-Increment (i++)
                if (i + 1 < expressionChars.length && expressionChars[i] == plusSymbol && expressionChars[i + 1] == plusSymbol) {
                    int currentValue = variableMemoryMap.getOrDefault(variableName, 0);
                    variableMemoryMap.put(variableName, currentValue + 1); // Side Effect: Update memory
                    valueStack.push(currentValue); // Push OLD value for calculation
                    // Skip "++"
                    i += 2;
                    // Correct loop index
                    i--;
                }
                // Check for Post-Decrement (i--)
                else if (i + 1 < expressionChars.length && expressionChars[i] == minusSymbol && expressionChars[i + 1] == minusSymbol) {
                    int currentValue = variableMemoryMap.getOrDefault(variableName, 0);
                    variableMemoryMap.put(variableName, currentValue - 1); // Side Effect: Update memory
                    valueStack.push(currentValue); // Push OLD value
                    // Skip "--"
                    i += 2;
                    // Correct loop index
                    i--;
                }
                // Just a normal variable
                else {
                    valueStack.push(variableMemoryMap.getOrDefault(variableName, 0));
                    // Correct loop index
                    i--;
                }
            }

            // --- Case 4: Pre-Fix Operators (++i, --i) OR Standard Math (+, -) ---
            else if (currentChar == plusSymbol || currentChar == minusSymbol) {

                // [FIX] Check if this is *really* a Pre-Fix Operator.
                // It is a prefix operator ONLY if it is followed by a Variable (Letter).
                boolean isPrefixOperator = false;
                if (i + 1 < expressionChars.length && expressionChars[i + 1] == currentChar) {
                    // Check ahead: is the character AFTER the ++/-- a letter?
                    if (i + 2 < expressionChars.length && Character.isLetter(expressionChars[i + 2])) {
                        isPrefixOperator = true;
                    }
                }

                if (isPrefixOperator) {
                    boolean isIncrement = (currentChar == plusSymbol);
                    i += 2; // Skip the operator chars (e.g. "++")

                    // Parse the variable name immediately following the operator
                    StringBuilder variableBuilder = new StringBuilder();
                    while (i < expressionChars.length && Character.isLetterOrDigit(expressionChars[i])) {
                        variableBuilder.append(expressionChars[i++]);
                    }
                    String variableName = variableBuilder.toString();

                    // Perform the update
                    int currentValue = variableMemoryMap.getOrDefault(variableName, 0);
                    int newValue = isIncrement ? currentValue + 1 : currentValue - 1;
                    // Side Effect: Update memory
                    variableMemoryMap.put(variableName, newValue);
                    // Push NEW value for calculation
                    valueStack.push(newValue);

                    // Correct loop index
                    i--;
                }
                // It is a Standard Math Operator (+ or -)
                else {
                    while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), currentChar)) {
                        applyTopOperation(operatorStack.pop(), valueStack);
                    }
                    operatorStack.push(currentChar);
                }
            }

            // --- Case 5: Parentheses ---
            else if (currentChar == OPENED_PARENTHESES) {
                operatorStack.push(currentChar);
            } else if (currentChar == CLOSED_PARENTHESES) {
                while (!operatorStack.isEmpty() && operatorStack.peek() != OPENED_PARENTHESES) {
                    applyTopOperation(operatorStack.pop(), valueStack);
                }
                // Pop the opening '('
                operatorStack.pop();
            }

            // --- Case 6: High Priority Math (*, /, ^) ---
            else if (Operator.highPriorityMathOperator(currentChar)) {
                while (!operatorStack.isEmpty() && hasPrecedence(operatorStack.peek(), currentChar)) {
                    applyTopOperation(operatorStack.pop(), valueStack);
                }
                operatorStack.push(currentChar);
            }
        }

        // Final cleanup: Process remaining operators
        while (!operatorStack.isEmpty()) {
            applyTopOperation(operatorStack.pop(), valueStack);
        }

        // [VALIDATION STEP] Ensure exactly one result remains
        if (valueStack.size() != 1) {
            throw new IllegalArgumentException("Invalid Expression: Malformed input (e.g., missing operator or operand)");
        }

        return valueStack.pop();
    }

    /**
     * Checks if the operator on top of the stack has higher or equal precedence
     * compared to the current operator being read.
     * NOTE: stack operator is in ascending order of precedence.
     *
     * @param stackOperator   The operator currently on the stack
     * @param currentOperator The new operator being parsed
     * @return true if stackOperator should be executed first
     */
    private boolean hasPrecedence(char stackOperator, char currentOperator) {
        if (stackOperator == OPENED_PARENTHESES || stackOperator == CLOSED_PARENTHESES)
            return false;

        // Power (^) is Right-Associative: 2^3^2 -> 2^(3^2).
        // If we see ^ on stack and current is ^, we return false to delay execution.
        char powerSymbol = Operator.POWER.getSymbol();
        if (currentOperator == powerSymbol && stackOperator == powerSymbol) return false;

        // Power has the highest precedence
        if (stackOperator == powerSymbol) return true;

        // Multiplication and Division have higher precedence than Plus and Minus
        if ((stackOperator == Operator.MULTIPLY.getSymbol() || stackOperator == Operator.DIVIDE.getSymbol())
                && (currentOperator == Operator.PLUS.getSymbol() || currentOperator == Operator.MINUS.getSymbol())) {
            return true;
        }

        return false;
    }

    /**
     * Applies the given operator to the top two values on the value stack.
     *
     * @param operator
     * @param valueStack
     */
    private void applyTopOperation(char operator, Deque<Integer> valueStack) {
        // [VALIDATION STEP] Check if we have enough operands
        if (valueStack.size() < 2) {
            throw new IllegalArgumentException("Invalid Expression: Missing operand for operator '" + operator + "'");
        }

        int rightOperand = valueStack.pop();
        int leftOperand = valueStack.pop();

        switch (Operator.get(operator)) {
            case PLUS:
                valueStack.push(leftOperand + rightOperand);
                break;
            case MINUS:
                valueStack.push(leftOperand - rightOperand);
                break;
            case MULTIPLY:
                valueStack.push(leftOperand * rightOperand);
                break;
            case DIVIDE:
                if (rightOperand == 0) throw new UnsupportedOperationException("Math error: Division by zero");
                valueStack.push(leftOperand / rightOperand);
                break;
            case POWER:
                valueStack.push((int) Math.pow(leftOperand, rightOperand));
                break;
            default:
                throw new IllegalArgumentException("Unknown operator encountered: " + operator);
        }
    }

    /**
     * Updates the variable map based on the specific assignment operator.
     * * @param targetVar The variable name to update (L-Value)
     *
     * @param operator      The operator used (=, +=, -=, etc.)
     * @param valueToAssign The result of the math expression (R-Value)
     */
    private void performAssignment(String targetVar, String operator, int valueToAssign) {
        int currentValue = variableMemoryMap.getOrDefault(targetVar, 0);
        int finalValue = 0;

        if (operator.equals(EQUALS)) {
            finalValue = valueToAssign;
        } else {
            CompoundOperator compoundOperator = CompoundOperator.get(operator);
            switch (compoundOperator) {
                case PLUS_ASSIGN:
                    finalValue = currentValue + valueToAssign;
                    break;
                case MINUS_ASSIGN:
                    finalValue = currentValue - valueToAssign;
                    break;
                case MULTIPLY_ASSIGN:
                    finalValue = currentValue * valueToAssign;
                    break;
                case DIVIDE_ASSIGN:
                    if (valueToAssign == 0) {
                        throw new UnsupportedOperationException("Cannot divide by zero during assignment");
                    }
                    finalValue = currentValue / valueToAssign;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown assignment operator: " + operator);
            }
        }
        variableMemoryMap.put(targetVar, finalValue);
    }

    /**
     * Formatting helper to print the memory map as (key=val,key=val).
     */
    private void printMemoryState() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        variableMemoryMap.forEach((variableName, value) -> {
            stringBuilder.append(variableName).append("=").append(value).append(",");
        });
        // CHECK: If map wasn't empty, we have a trailing comma. Delete it.
        if (!variableMemoryMap.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        stringBuilder.append(")");
        log.info(stringBuilder.toString());
    }

}