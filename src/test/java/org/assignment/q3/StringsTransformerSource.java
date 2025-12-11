package org.assignment.q3;

import java.util.ArrayList;
import java.util.List;

public class StringsTransformerSource {

    private List<String> data = new ArrayList<String>();

    public StringsTransformerSource(List<String> startingData) {
        this.data = startingData;
    }

    private void forEach(StringFunction function) {
        List<String> newData = new ArrayList<String>();
        for (String str : data) {
            newData.add(function.transform(str));
        }
        data = newData;
    }

    public List<String> transform(List<StringFunction> functions) throws InterruptedException {
        List<Thread> threads = new ArrayList<Thread>();
        for (final StringFunction f : functions) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    forEach(f);
                }
            }));
        }
        for (Thread t : threads) {
            t.join();
        }
        return data;
    }

    public static interface StringFunction {
        public String transform(String str);
    }

}
