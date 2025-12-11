package org.assignment.q2;

import java.util.Date;
import java.util.List;

public class MyClassSource {

    private Date m_time;
    private String m_name;
    private List<Long> m_numbers;
    private List<String> m_strings;

    public MyClassSource(Date time, String name, List<Long> numbers, List<String> strings) {
        m_time = time;
        m_name = name;
        m_numbers = numbers;
        m_strings = strings;
    }

    public boolean equals(Object obj) {
        if (obj instanceof MyClassSource) {
            return m_name.equals(((MyClassSource) obj).m_name);
        }
        return false;
    }

    public String toString() {
        String out = m_name;
        for (long item : m_numbers) {
            out += " " + item;
        }
        return out;
    }

    public void removeString(String str) {
        for (int i = 0; i < m_strings.size(); i++) {
            if (m_strings.get(i).equals(str)) {
                m_strings.remove(i);
            }
        }
    }

    public boolean containsNumber(long number) {
        for (long num : m_numbers) {
            if (num == number) {
                return true;
            }
        }
        return false;
    }

    public boolean isHistoric() {
        return m_time.before(new Date());
    }

}