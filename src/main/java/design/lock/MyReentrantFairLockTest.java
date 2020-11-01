package design.lock;

import java.util.ArrayList;
import java.util.List;

public class MyReentrantFairLockTest {
    public static void main(String[] args) throws InterruptedException {
        MyReentrantLock lock = new MyReentrantLock(true);

        Runnable r = () -> {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            while (!thread.isInterrupted()) {
                try {
                    lock.lock();
                    System.out.printf("Thread: [%s] acquired the lock, executing the task %n", threadName);
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    System.out.printf("Thread: [%s] interrupted %n", threadName);
                    thread.interrupt();
                } finally {
                    lock.unlock();
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

        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Exiting");
    }
}
