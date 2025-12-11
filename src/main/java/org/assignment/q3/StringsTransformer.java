package org.assignment.q3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Class to transform a list of strings using a pipeline of transformation functions.
 * The transformations are applied in parallel using a Thread Pool, with the pool size
 * determined by the nature of the workload (CPU-bound or IO-bound).
 */
public class StringsTransformer {

    private static final int IO_THREAD_NUM = 50;
    private static final int BATCH_SIZE = 10;

    /**
     * Enum to define the nature of the transformation tasks.
     * This helps in deciding the optimal size of the Thread Pool.
     */
    public enum WorkloadType {
        /**
         * Use when transformations involve heavy calculations (e.g., encryption, regex, hashing).
         * In this case, the CPU is the bottleneck.
         */
        CPU_BOUND,

        /**
         * Use when transformations involve waiting for external resources (e.g., DB queries, HTTP requests).
         * In this case, threads spend most of their time in a WAITING state, leaving the CPU free.
         */
        IO_BOUND
    }

    private final List<String> data;
    private final ExecutorService executionService;

    /**
     * Constructor initializes the Thread Pool based on the specific workload type.
     *
     * @param inputStrings The list of strings to process.
     * @param workloadType The nature of the work (affects concurrency strategy).
     */
    public StringsTransformer(List<String> inputStrings, WorkloadType workloadType) {
        // Defensive copy to protect against external modifications
        this.data = new ArrayList<>(inputStrings);

        int availableCores = Runtime.getRuntime().availableProcessors();

        if (workloadType == WorkloadType.CPU_BOUND) {
            // Strategy: Keep thread count close to the physical core count.
            // Why? Adding more threads than cores causes excessive Context Switching, which hurts performance.
            // We add +1 to handle occasional pauses (like Page Faults).
            this.executionService = Executors.newFixedThreadPool(availableCores + 1);
        } else {
            // Strategy: Use a much larger pool for IO-bound tasks.
            // Why? Since threads spend most time waiting (blocking), we need more threads
            // to ensure the CPU is kept busy processing other tasks while some threads wait.
            // Ideally, this number should come from configuration, but 50-100 is a common starting point.
            this.executionService = Executors.newFixedThreadPool(IO_THREAD_NUM);

        }
    }

    /**
     * Applies the list of transformation functions to the data.
     *
     * @param transformationPipeline The list of functions to apply sequentially to each string.
     * @return A list of transformed strings.
     * @throws InterruptedException If the thread is interrupted while waiting.
     * @throws ExecutionException   If a transformation function throws an exception.
     */
    public List<String> applyTransformations(List<StringFunction> transformationPipeline)
            throws InterruptedException, ExecutionException {

        // We need to keep the Futures in order to reconstruct the list correctly later
        List<Future<List<String>>> futures = new ArrayList<>();
        int totalSize = data.size();

        // --- PHASE 1: SUBMIT TASKS (PRODUCER) ---
        for (int i = 0; i < totalSize; i += BATCH_SIZE) {

            int end = Math.min(i + BATCH_SIZE, totalSize);
            // Must create a copy for the thread to use safely
            List<String> batchSubList = data.subList(i, end);

            // Create the task logic
            Callable<List<String>> task = () -> {
                List<String> batchResults = new ArrayList<>(batchSubList.size());
                for (String str : batchSubList) {
                    String processed = str;
                    for (StringFunction f : transformationPipeline) {
                        processed = f.transform(processed);
                    }
                    batchResults.add(processed);
                }
                return batchResults;
            };

            // CRITICAL CHANGE:
            // We use .submit() instead of adding to a list and calling invokeAll().
            // This starts the thread IMMEDIATELY. The Main Thread continues to the next loop iteration
            // while the Worker Thread is already processing this batch.
            Future<List<String>> future = executionService.submit(task);

            // We save the Future to collect the result later
            futures.add(future);
        }

        // --- PHASE 2: COLLECT RESULTS (CONSUMER) ---
        List<String> finalResults = new ArrayList<>(totalSize);

        // Iterate over the futures in the order they were submitted.
        // Even if batch #5 finishes before batch #1, we will wait for #1 first here,
        // preserving the original list order.
        for (Future<List<String>> future : futures) {
            finalResults.addAll(future.get()); // Blocks only if this specific batch isn't ready yet
        }

        executionService.shutdown();
        return finalResults;
    }

    /**
     * Functional interface for string transformation functions.
     */
    public interface StringFunction {
        String transform(String str);
    }

}