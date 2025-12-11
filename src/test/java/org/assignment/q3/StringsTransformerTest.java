package org.assignment.q3;

import org.assignment.q3.StringsTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for StringsTransformer.
 * This class should contain unit tests to verify the functionality of the StringsTransformer class,
 * including tests for both CPU-bound and IO-bound workloads.
 */
public class StringsTransformerTest {

    // --- SETUP: Hack the Batch Size to 1 ---
    @BeforeEach
    void setupBatchSize() throws Exception {
        // 1. Get the private field
        Field batchField = StringsTransformer.class.getDeclaredField("BATCH_SIZE");
        batchField.setAccessible(true);

        // 2. Remove the 'final' modifier (Only works heavily on Java 8-11, restricted in 12+)
        // This trick convinces the JVM to let us update a constant.
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        // Perform a bitwise AND with the complement (NOT) of Modifier.FINAL.
        // This effectively "turns off" the bit that represents 'final' in the integer,
        // forcing the JVM to treat the field as mutable (non-final) so we can modify it.
        modifiersField.setInt(batchField, batchField.getModifiers() & ~Modifier.FINAL);

        // 3. Set the value to 1
        // Now the code will treat every single item as a separate "batch" task
        batchField.set(null, 1);

        System.out.println("TEST SETUP: BATCH_SIZE is now forced to 1.");
    }

    // --- 1. Test CPU Bound Strategy (Fast execution) ---
    @Test
    void testCpuBoundStrategy_Correctness() throws InterruptedException, ExecutionException {
        // Input: ["a", "b", "c"]
        List<String> input = Arrays.asList("a", "b", "c");

        // Define transformations: UpperCase -> Add "!"
        List<StringsTransformer.StringFunction> pipeline = Arrays.asList(
                String::toUpperCase, // "A"
                s -> s + "!"         // "A!"
        );

        // Initialize with CPU_BOUND strategy
        StringsTransformer transformer =
                new StringsTransformer(input, StringsTransformer.WorkloadType.CPU_BOUND);

        List<String> result = transformer.applyTransformations(pipeline);

        // Verify size
        assertEquals(3, result.size());

        // Verify Content
        assertEquals("A!", result.get(0));
        assertEquals("B!", result.get(1));
        assertEquals("C!", result.get(2));
    }

    // --- 2. Test IO Bound Strategy (Simulated Slow Execution) ---
    @Test
    void testIoBoundStrategy_SimulatedLatency() throws InterruptedException, ExecutionException {
        List<String> input = Arrays.asList("1", "2", "3", "4", "5");

        // Define a "Slow" transformation (Simulating DB call)
        List<StringsTransformer.StringFunction> pipeline = Arrays.asList(
                s -> {
                    try {
                        // Simulate IO wait (10ms)
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Processed-" + s;
                }
        );

        // Initialize with IO_BOUND strategy (Larger Thread Pool)
        StringsTransformer transformer =
                new StringsTransformer(input, StringsTransformer.WorkloadType.IO_BOUND);

        long startTime = System.currentTimeMillis();
        List<String> result = transformer.applyTransformations(pipeline);
        long endTime = System.currentTimeMillis();

        // Verify Correctness
        assertEquals("Processed-1", result.get(0));
        assertEquals("Processed-5", result.get(4));

        // Verify that it actually ran (result is not null)
        assertEquals(5, result.size());

        System.out.println("IO Test took: " + (endTime - startTime) + "ms");
    }

    // --- 3. Test Order Preservation (Critical for Multi-Threading) ---
    @Test
    void testOrderPreservation() throws InterruptedException, ExecutionException {
        // Input with distinct items to check order
        List<String> input = Arrays.asList("First", "Second", "Third", "Fourth");

        // Simple identity transformation
        List<StringsTransformer.StringFunction> pipeline = Arrays.asList(s -> s);

        StringsTransformer transformer =
                new StringsTransformer(input, StringsTransformer.WorkloadType.CPU_BOUND);

        List<String> result = transformer.applyTransformations(pipeline);

        // Assert that the output order matches the input order EXACTLY
        assertEquals("First", result.get(0));
        assertEquals("Second", result.get(1));
        assertEquals("Third", result.get(2));
        assertEquals("Fourth", result.get(3));
    }

    // --- 4. Test Empty Input Safety ---
    @Test
    void testEmptyInput() throws InterruptedException, ExecutionException {
        List<String> input = Arrays.asList(); // Empty list

        StringsTransformer transformer =
                new StringsTransformer(input, StringsTransformer.WorkloadType.CPU_BOUND);

        List<String> result = transformer.applyTransformations(Arrays.asList(String::toUpperCase));

        assertTrue(result.isEmpty(), "Result should be empty if input is empty");
    }

}
