package locks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {

    public static void main(String[] args) throws InterruptedException {

        Counter counter = new Counter();
        Runnable runnable = () -> {

            try {
                System.out.println(counter.getVal());
                Thread.sleep(2*1000);
                System.out.println(counter.getVal());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        List<Thread> readers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            readers.add(new Thread(runnable, "reader-thread-" + i));
        }

        Thread writer = new Thread(() -> {
            try {
                counter.increment();
                Thread.sleep(2*1000);
                counter.increment();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "writer-thread");

        writer.start();
        readers.forEach(Thread::start);

        writer.join();
        for (Thread r : readers) {
            r.join();
        }

        System.out.println("Main thread exiting");
    }

    private static class Counter {
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private AtomicInteger integer = new AtomicInteger(0);

        public int getVal() {
            try {
                lock.readLock().lock();
                System.out.printf("Thread [%s] acquired readlock%n", Thread.currentThread().getName());
                Thread.sleep(2 * 1000);
                lock.readLock().unlock();
                return integer.get();
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
                return 0;
            }
        }

        public void increment() {
            try {
                lock.writeLock().lock();
                integer.set(integer.incrementAndGet());
                System.out.printf("Thread [%s] acquired writelock%n", Thread.currentThread().getName());
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
