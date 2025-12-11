package org.assignment.q2;

import org.assignment.q2.MyClass;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MyClass}
 * In this test suite, we compare the behavior of {@link MyClass} (new implementation)
 * against {@link MyClassSource} (original implementation) to ensure identical functionality,
 * while also verifying bug fixes and improvements.
 */
public class MyClassTest {

    /**
     * testing {@link MyClass#toString()} method for identical output
     */
    @Test
    void testToString() {
        // Setup Data
        Date now = new Date();
        String name = "TestObj";
        List<Long> nums = Arrays.asList(100L, 200L, 300L, 200L);
        List<String> strs = new ArrayList<>(Arrays.asList("A", "B", "A", "C"));

        // Create Old and New instances
        MyClassSource oldObj = new MyClassSource(now, name, nums, strs);
        MyClass newObj = new MyClass(now, name, nums, strs);

        // Assert output is identical
        assertEquals(oldObj.toString(), newObj.toString(),
                "toString output should be identical for both classes");
    }

    /**
     * testing {@link MyClass#containsNumber(long)} method for identical behavior
     */
    @Test
    void testContainsNumber() {
        Date now = new Date();
        List<Long> nums = Arrays.asList(5L, 10L, 5L);

        MyClassSource oldObj = new MyClassSource(now, "Name", nums, new ArrayList<>());
        MyClass newObj = new MyClass(now, "Name", nums, new ArrayList<>());

        // Check True case
        assertTrue(oldObj.containsNumber(5L));
        assertTrue(newObj.containsNumber(5L));

        // Check False case
        assertFalse(oldObj.containsNumber(99L));
        assertFalse(newObj.containsNumber(99L));
    }

    /**
     * testing {@link MyClass#isHistoric()} method for identical behavior
     */
    @Test
    void testIsHistoric() {
        // Date in the past (1970)
        Date pastDate = new Date(0);

        MyClassSource oldObj = new MyClassSource(pastDate, "History", new ArrayList<>(), new ArrayList<>());
        MyClass newObj = new MyClass(pastDate, "History", new ArrayList<>(), new ArrayList<>());

        assertEquals(oldObj.isHistoric(), newObj.isHistoric(),
                "Both should identify past dates correctly");
    }

    /**
     * testing {@link MyClass#removeString(String)} method to verify bug fix
     *
     * @throws Exception
     */
    @Test
    void testRemoveString_BugFix() throws Exception {
        // Scenario: The "Skip Element" bug.
        // List: ["A", "A", "B"]. We want to remove "A".
        // Old Code: Removes index 0. Index 1 becomes "A". Loop increments. Skips "A". Result: ["A", "B"].
        // New Code: Should remove all "A"s. Result: ["B"].

        List<String> inputList = new ArrayList<>(Arrays.asList("A", "A", "B"));
        Date now = new Date();

        // 1. Run OLD Class
        // Note: We must pass a copy of the list because the old class doesn't do defensive copy!
        MyClassSource oldObj = new MyClassSource(now, "Old", new ArrayList<>(), new ArrayList<>(inputList));
        oldObj.removeString("A");

        // 2. Run NEW Class
        MyClass newObj = new MyClass(now, "New", new ArrayList<>(), new ArrayList<>(inputList));
        newObj.removeString("A");

        // 3. Compare Results
        List<String> oldResult = getStringsViaReflection(oldObj);
        List<String> newResult = getStringsViaReflection(newObj);

        System.out.println("Old Class Result (Buggy): " + oldResult); // Expect [A, B]
        System.out.println("New Class Result (Fixed): " + newResult); // Expect [B]

        // Assert that the old class FAILED (still contains "A")
        assertTrue(oldResult.contains("A"), "Original class should demonstrate the skipping bug");

        // Assert that the new class SUCCEEDED (no "A")
        assertFalse(newResult.contains("A"), "New class should have fixed the removing bug");
        assertEquals(1, newResult.size());
        assertEquals("B", newResult.get(0));
    }

    /**
     * testing {@link MyClass#equals(Object)} method for null safety fix
     */
    @Test
    void testEquals_NullSafetyFix() {
        Date now = new Date();
        // Name is NULL
        String name = null;

        MyClassSource oldObj = new MyClassSource(now, name, new ArrayList<>(), new ArrayList<>());
        MyClass newObj = new MyClass(now, name, new ArrayList<>(), new ArrayList<>());

        // Old class crashes on equals with null name
        assertThrows(NullPointerException.class, () -> {
            oldObj.equals(new MyClassSource(now, "Other", null, null));
        }, "Old class should crash when name is null");

        // New class handles it gracefully
        assertDoesNotThrow(() -> {
            boolean res = newObj.equals(new MyClass(now, "Other", null, null));
            assertFalse(res);
        }, "New class should handle null safely");
    }

    /**
     * Helper to get the private m_strings field via reflection
     *
     * @param instance
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringsViaReflection(Object instance) throws Exception {
        Field field = instance.getClass().getDeclaredField("m_strings");
        field.setAccessible(true); // Allow access to private field
        return (List<String>) field.get(instance);
    }

}
