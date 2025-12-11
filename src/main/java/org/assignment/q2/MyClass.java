package org.assignment.q2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a data structure with time, name, numbers, and strings.
 * It provides methods to manipulate and query its data.
 */
public class MyClass {

    private static final String SPACE = " ";

    private final Instant m_time;
    private final String m_name;
    private final List<Long> m_numbers;
    private final List<String> m_strings;

    public MyClass(Date time, String name, List<Long> numbers, List<String> strings) {
        if (time == null) {
            throw new IllegalArgumentException("time cannot be null");
        }
        this.m_time = Instant.ofEpochMilli(time.getTime());
        this.m_name = name;
        this.m_numbers = numbers != null ? new ArrayList<>(numbers) : new ArrayList<>();
        this.m_strings = strings != null ? new ArrayList<>(strings) : new ArrayList<>();
    }

    /**
     * compare this object with another for equality based on {@link MyClass#m_name}
     *
     * @param other the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        // Use Objects.equals to handle nulls safely
        return Objects.equals(m_name, ((MyClass) other).m_name);
    }

    /**
     * Generate hash code based on name
     * without this method {@link #equals(Object)} would be broken in hash-based collections
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(m_name);
    }

    /**
     * String representation of the object
     *
     * @return
     */
    public String toString() {
        // StringBuilder is mutable and more efficient for concatenation in loops than String
        StringBuilder stringBuilder = new StringBuilder(m_name);
        for (long number : m_numbers) {
            stringBuilder.append(SPACE).append(number);
        }
        return stringBuilder.toString();
    }

    /**
     * remove all occurrences of the string from the list
     *
     * @param string
     */
    public void removeString(String string) {
        // in case in the future we will want to print or access m_strings from multiple threads, we may consider
        // CopyOnWriteArrayList or other concurrent collections
        synchronized (m_strings) {
            // Safe removal to avoid skipping elements
            m_strings.removeIf(s -> Objects.equals(s, string));
        }
    }

    /**
     * Check if the number is contained in the list
     *
     * @param number
     * @return
     */
    public boolean containsNumber(long number) {
        return m_numbers.contains(number);
    }

    /**
     * Check if the time is historic(less than current time)
     *
     * @return
     */
    public boolean isHistoric() {
        return m_time.isBefore(Instant.now());
    }

}