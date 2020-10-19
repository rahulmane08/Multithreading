package locks;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock(true);
        Runnable r = () ->
        {
            while (true) {
                try {
                    lock.lockInterruptibly();
                    System.out.printf("Thread: [%s] got the lock%n", Thread.currentThread().getName());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.printf("Thread: [%s] interrupted%n", Thread.currentThread().getName());
                    break;
                } finally {
                    try {
                        System.out.printf("Thread: [%s] released the lock%n", Thread.currentThread().getName());
                        System.out.println();
                        lock.unlock();
                    } catch (IllegalMonitorStateException ex) {

                    }
                }
            }
        };

        Thread t1 = new Thread(r, "worker-thread-1");
        Thread t2 = new Thread(r, "worker-thread-2");
        t1.start();
        t2.start();

        Thread.sleep(10 * 1000);
        t1.interrupt();
        t2.interrupt();

        t1.join();
        t2.join();
    }
}
