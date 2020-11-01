package locks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockFairnessTest {

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
                    if (lock.isHeldByCurrentThread()) {
                        // System.out.printf("Thread: [%s] released the lock%n", Thread.currentThread().getName());
                        lock.unlock();
                    }
                }
            }
            System.out.printf("Thread: [%s] finishing %n", Thread.currentThread().getName());
        };

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i< 10; i++) {
            threads.add(new Thread(r, "T"+i));
        }
        threads.forEach(Thread::start);
        Thread.sleep(60000);
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Exiting");
    }
}
