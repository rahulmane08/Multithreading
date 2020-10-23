package synchronizers.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreTimedAcquireTest {

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        Runnable r = () -> {
            try {
                boolean acquired = semaphore.tryAcquire(3, TimeUnit.SECONDS);
                if (acquired) {
                    System.out.printf("Thread: [%s] got semaphore%n", Thread.currentThread().getName());
                    Thread.sleep(2 * 1000);
                    semaphore.release();
                } else {
                    System.out.printf("Thread: [%s] couldnt semaphore%n", Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                System.out.printf("Thread: [%s] got interrupted%n", Thread.currentThread().getName());
            }
        };
        semaphore.acquire();
        Thread t = new Thread(r, "worker-thread");
        t.start();
        Thread.sleep(10 * 1000);
        semaphore.release();
        t.join();
        System.out.println("exiting");
    }
}
