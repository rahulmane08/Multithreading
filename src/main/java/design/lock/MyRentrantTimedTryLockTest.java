package design.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyRentrantTimedTryLockTest {
    public static void main(String[] args) throws InterruptedException {
        MyReentrantLock lock = new MyReentrantLock();

        Runnable r = () -> {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            while (!thread.isInterrupted()) {
                try {
                    if (lock.tryLock(3, TimeUnit.SECONDS)) {
                        System.out.printf("Thread: [%s] acquired lock %n", Thread.currentThread().getName());
                        Thread.sleep(5000);
                        System.out.printf("Thread: [%s] executed task %n", Thread.currentThread().getName());
                    } else {
                        System.out.printf("Thread: [%s] COULDNT acquire lock %n", Thread.currentThread().getName());
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException ex) {
                    System.out.printf("Thread: [%s] interrupted %n",
                            threadName);
                    thread.interrupt();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        System.out.printf("Thread: [%s] releasing the lock %n", threadName);
                        lock.unlock();
                    }
                }
            }
            System.out.printf("Thread: [%s] exiting %n", threadName);
        };

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            threads.add(new Thread(r, "T" + i));
        }
        threads.forEach(Thread::start);

        Thread.sleep(20000);
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Exiting");
    }
}
