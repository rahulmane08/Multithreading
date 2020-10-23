package executors.forkjoin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

public class FJPWithRecursiveAction {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        int n = 100;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        ForkJoinTask<Void> result = forkJoinPool.submit(new ArrayPrinter(0, n - 1, arr, 5));
        result.get();
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Exiting");
    }

    @AllArgsConstructor
    private static class ArrayPrinter extends RecursiveAction {
        private final int start;
        private final int end;
        private final int[] arr;
        private final int limit;

        @Override
        protected void compute() {
            if (end - start <= limit) {
                System.out.printf("Thread: [%s] printing arr: %s %n",
                        Thread.currentThread().getName(), Arrays.toString(Arrays.copyOfRange(arr, start, end + 1)));
                return;
            }

            for (RecursiveAction subtask : createSubtasks()) {
                subtask.fork();
            }
        }

        private List<ArrayPrinter> createSubtasks() {
            int mid = start + (end - start) / 2;
            ArrayPrinter task1 = new ArrayPrinter(start, mid, arr, limit);
            ArrayPrinter task2 = new ArrayPrinter(mid + 1, end, arr, limit);
            return Arrays.asList(
                    task1, task2);
        }

        @Override
        public String toString() {
            return "(" + start + ", " + end + ")";
        }
    }
}
