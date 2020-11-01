package synchronizers.semaphore;

import java.util.concurrent.Semaphore;

public class SemaphoreInterruptionTest {

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        Runnable runnable = () -> {
            try {
                semaphore.acquire(5);
                System.out.printf("Thread: {%s} executed %n", Thread.currentThread().getName());
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.printf("Thread: {%s} interrupted %n", Thread.currentThread().getName());
            } finally {
                semaphore.release();
            }
        };
        Thread t1 = new Thread(runnable, "worker-1");
        Thread t2 = new Thread(runnable, "worker-2");

        semaphore.acquire();
        t1.start();
        t2.start();
        Thread.sleep(5000);
        semaphore.release();
        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println("exiting");
    }
}
