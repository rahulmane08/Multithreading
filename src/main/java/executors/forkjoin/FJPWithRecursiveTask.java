package executors.forkjoin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

public class FJPWithRecursiveTask {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        int n = 100;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        ForkJoinTask<Integer> task = forkJoinPool.submit(new ArrayAdder(0, n - 1, arr, 5));
        Integer finalSum = task.get();
        System.out.println("Final Sum = " + finalSum);
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Exiting");
    }

    @AllArgsConstructor
    private static class ArrayAdder extends RecursiveTask<Integer> {
        private final int start;
        private final int end;
        private final int[] arr;
        private final int limit;

        @Override
        protected Integer compute() {
            if (end - start < limit) {
                int sum = 0;
                for (int i = start; i <= end; i++) {
                    sum += arr[i];
                }
                return sum;
            }
            List<ArrayAdder> subtasks = createSubtasks();
            subtasks.forEach(ArrayAdder::fork);
            return subtasks.stream().map(ArrayAdder::join).reduce(0, Integer::sum);
        }

        private List<ArrayAdder> createSubtasks() {
            int mid = start + ((end - start) / 2);
            ArrayAdder task1 = new ArrayAdder(start, mid, arr, limit);
            ArrayAdder task2 = new ArrayAdder(mid + 1, end, arr, limit);
            return Arrays.asList(task1, task2);
        }
    }
}
