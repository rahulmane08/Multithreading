package futures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.AllArgsConstructor;
import lombok.ToString;

public class FutureTaskTest {
    public static void main(String[] args) throws InterruptedException {
        Task t1 = new Task(1,4);
        Task t2 = new Task(2,8);
        Task t3 = new Task(3,10);
        Task t4 = new Task(4,15);

        List<Task> tasks = Arrays.asList(t1, t2, t3, t4);
        List<FutureTask<String>> futureTasks = new ArrayList<FutureTask<String>>();
        ExecutorService service = Executors.newCachedThreadPool();
        for (Task t : tasks)
            futureTasks.add((FutureTask<String>) service.submit(t));

        for (FutureTask<String> ft : futureTasks) {
            String threadName = Thread.currentThread().getName();
            try {
                System.out.printf("Thread: [%s] got the result = %s %n", threadName, ft.get(4, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                System.out.printf("Thread: [%s] was interrupted %n", threadName);
            } catch (ExecutionException e) {
                System.out.printf("Thread: [%s] encountered execution exception = %s %n", threadName, e.getMessage());
            } catch (TimeoutException e) {
                ft.cancel(true);
                System.out.printf("Future timed out, Thread: [%s] cancelling the task %n", threadName);
            }
        }
        futureTasks.clear();
        service.shutdown();
    }

    @AllArgsConstructor
    @ToString
    private static class Task implements Callable<String> {
        private final int id;
        private final int time;

        @Override
        public String call() throws Exception {
            System.out.printf("Thread : [%s] executing the task: %s %n", Thread.currentThread().getName(), this);
            Thread.sleep(this.time * 1000);
            if (id == 3) {
                throw new RuntimeException("Illegal thread not allowed");
            }
            return Thread.currentThread().getName() + " task";
        }

    }
}
