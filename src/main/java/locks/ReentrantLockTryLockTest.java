package locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTryLockTest {
    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Runnable r = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (lock.tryLock(4, TimeUnit.SECONDS)) {
                        System.out.printf("Thread: [%s] acquired lock %n", Thread.currentThread().getName());
                        Thread.sleep(10000);
                        System.out.printf("Thread: [%s] executed task %n", Thread.currentThread().getName());
                    } else {
                        System.out.printf("Thread: [%s] COULDNT acquire lock %n", Thread.currentThread().getName());
                    }
                } catch (InterruptedException ex) {
                    System.out.printf("Thread: [%s] interrupted%n", Thread.currentThread().getName());
                    Thread.currentThread().interrupt();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        };

        Thread t1 = new Thread(r, "T1");
        Thread t2 = new Thread(r, "T2");
        t1.start();
        t2.start();
        Thread.sleep(20 * 1000);
        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();
        System.out.println("Exiting");
    }

    public static void handleInterrupts(InterruptedException ie) {
        System.out.println(Thread.currentThread().getName() + " interrrupted " + ie.toString());
        Thread.currentThread().interrupt();
    }
}
