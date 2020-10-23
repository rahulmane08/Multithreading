package executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorExceptionTest {

    public static void main(String[] args) {
        final int x = 0;
        Runnable runnable = () -> {
            System.out.printf("Thread [%s] running %n", Thread.currentThread().getName());
            if (x == 0) {
                throw new NullPointerException();
            }
        };
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(runnable);
        service.shutdown();
    }
}
